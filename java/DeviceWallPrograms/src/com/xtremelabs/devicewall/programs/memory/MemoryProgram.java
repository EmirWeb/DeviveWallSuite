package com.xtremelabs.devicewall.programs.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xtremelabs.devicewall.programs.amqp.AmqpConnectionManager;
import com.xtremelabs.devicewall.programs.amqp.AmqpListener;
import com.xtremelabs.devicewall.protocol.AmqpConstants;
import com.xtremelabs.devicewall.protocol.CardFace;
import com.xtremelabs.devicewall.protocol.Data;
import com.xtremelabs.devicewall.protocol.Protocol;
import com.xtremelabs.devicewall.protocol.gamecontrol.GameControlMessageType;
import com.xtremelabs.devicewall.protocol.gamecontrol.request.ClientStartRequest;
import com.xtremelabs.devicewall.protocol.gamecontrol.response.ServerStartResponse;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierMessageType;
import com.xtremelabs.devicewall.protocol.identifier.data.MobileServerIdentifierData;
import com.xtremelabs.devicewall.protocol.identifier.response.MapIdentifierResponse;
import com.xtremelabs.devicewall.protocol.memory.Card;
import com.xtremelabs.devicewall.protocol.memory.Card.CardState;
import com.xtremelabs.devicewall.protocol.memory.DeviceData;
import com.xtremelabs.devicewall.protocol.memory.FaceUpCard;
import com.xtremelabs.devicewall.protocol.memory.MemoryAssign;
import com.xtremelabs.devicewall.protocol.memory.MemoryClick;
import com.xtremelabs.devicewall.protocol.memory.MemoryConfirm;
import com.xtremelabs.devicewall.protocol.memory.MemoryDeserializer;
import com.xtremelabs.devicewall.protocol.memory.MemoryFlip;
import com.xtremelabs.devicewall.protocol.memory.MemoryMessageType;

public class MemoryProgram {

	private AmqpConnectionManager amqpConnectionManager;
	private Gson sGson;

	private ConcurrentHashMap<Long, DeviceData> mDeviceData = new ConcurrentHashMap<Long, DeviceData>();
	private volatile ArrayList<Long> mConfirmedDevices = new ArrayList<Long>();

	private FaceUpCard mFaceUpCard1;
	private FaceUpCard mFaceUpCard2;

	private AmqpListener amqpListener = new AmqpListener() {

		@Override
		public void handleDelivery(String body) throws IOException {
			System.out.println("Memory received a message: " + body);

			final Protocol protocol = sGson.fromJson(body, Protocol.class);
			if (protocol == null)
				return;
			final Data data = protocol.getData();

			final String messageType = protocol.getType();

			if (IdentifierMessageType.getModelType(messageType) != IdentifierMessageType.EMPTY) {
				IdentifierMessageType message = IdentifierMessageType.getModelType(messageType);

				switch (message) {
				case MAP_SERVER_RESPONSE:
					handleMapResponse((MapIdentifierResponse) data);
					break;

				default:
					break;
				}
			} else if (GameControlMessageType.getModelType(messageType) != GameControlMessageType.EMPTY) {
				GameControlMessageType message = GameControlMessageType.getModelType(messageType);

				switch (message) {
				case CLIENT_START:
					final ClientStartRequest clientStartRequest = (ClientStartRequest) protocol.getData();
					if (AmqpConstants.MEMORY_SERVER_APP_NAME.equals(clientStartRequest.getApp()))
						handleStart();
					break;
				default:
					break;
				}
			} else {
				MemoryMessageType message = MemoryMessageType.getModelType(messageType);

				switch (message) {
				case CLICK:
					handleClick(protocol.getId(), (MemoryClick) data);
					break;
				case CONFIRM:
					handleConfirm(protocol.getId(), (MemoryConfirm) data);
				default:
					break;
				}
			}
		}

		@Override
		public void onConnected() {
		}

		@Override
		public void onDisconnected() {
		}
	};

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		new MemoryProgram();
	}

	public MemoryProgram() {
		try {

			final GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Protocol.class, new MemoryDeserializer());
			sGson = builder.create();

			amqpConnectionManager = new AmqpConnectionManager(amqpListener);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void handleConfirm(final Long id, MemoryConfirm data) {
		mConfirmedDevices.add(id);
	}

	protected void handleStart() {
		amqpConnectionManager.publishToServer(IdentifierMessageType.MAP_SERVER_REQUEST.toString(), "");
	}

	protected void handleMapResponse(MapIdentifierResponse response) {
		final Collection<MobileServerIdentifierData> mobileServerIdentifierData = response.getMap();
		mDeviceData = new ConcurrentHashMap<Long, DeviceData>();

		int numberOfCards = 0;
		for (MobileServerIdentifierData mobileData : mobileServerIdentifierData) {
			if (mobileData.isTablet())
				numberOfCards += 2;
			else
				numberOfCards++;
		}
		final boolean oddNumberOfCards = numberOfCards % 2 == 1;
		final int numberOfPairs = numberOfCards / 2;

		final List<Card> cards = new ArrayList<Card>();

		final CardFace[] values = CardFace.values();
		for (int index = 0; index < numberOfPairs; index++) {
			CardFace cardFace = values[index % values.length];
			cards.add(new Card(CardState.DOWN, cardFace));
			cards.add(new Card(CardState.DOWN, cardFace));
		}
		Collections.shuffle(cards);
		for (MobileServerIdentifierData mobileData : mobileServerIdentifierData) {
			if (!cards.isEmpty()) {
				final ArrayList<Card> deviceCards = new ArrayList<Card>();
				Card card = cards.remove(0);
				deviceCards.add(card);
				if (mobileData.isTablet() && !cards.isEmpty()) {
					card = cards.remove(0);
					deviceCards.add(card);
				}
				DeviceData deviceData = new DeviceData(mobileData, deviceCards);
				mDeviceData.put(deviceData.getId(), deviceData);
			}
		}
		mConfirmedDevices = new ArrayList<Long>();

		amqpConnectionManager.publishToAll(GameControlMessageType.SERVER_START.toString(), new ServerStartResponse(AmqpConstants.MEMORY_SERVER_APP_NAME).toJson().toString());

		new Thread(new Runnable() {
			public void run() {
				// time to wait for clients to respond
				int delay = 60;

				while (!isSubset(mobileServerIdentifierData, mConfirmedDevices) && delay-- > 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				System.out.println("Done waiting for confirmations: delay = " + delay + ", confirmed devices length: " + mConfirmedDevices.size() + ", full list size: " + mobileServerIdentifierData.size());

				assignCards();
			}
		}).start();

	}

	/**
	 * Returns true if every element id of mobileServerIdentifierData list is in
	 * confirmed devices.
	 * 
	 * @param mobileServerIdentifierData
	 *            List of mobile server identifiers
	 * @param mConfirmedDevices2
	 *            List of longs
	 * @return true if every element id of mobileServerIdentifierData list is in
	 *         confirmed devices.
	 */
	protected boolean isSubset(Collection<MobileServerIdentifierData> mobileServerIdentifierData, ArrayList<Long> mConfirmedDevices2) {

		if (mConfirmedDevices2.size() <= 0) {
			return false;
		}

		for (Long subsetId : mConfirmedDevices2) {
			boolean found = false;

			for (MobileServerIdentifierData setId : mobileServerIdentifierData) {
				if (setId.getId().equals(subsetId)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}

		return true;
	}

	// fill data structures with test data
	protected void assignCards() {
		for (final Long id : mDeviceData.keySet()) {
			final DeviceData deviceData = mDeviceData.get(id);
			final String queueName = deviceData.getQueueName();
			if (queueName != null) {
				final MemoryAssign memoryAssign = new MemoryAssign(id);
				memoryAssign.setPic_idsFromCard(deviceData.getCards());
				amqpConnectionManager.publishToBinding(Long.toString(id), MemoryMessageType.ASSIGN.toString(), memoryAssign.toJson().toString());
			}
		}

		mFaceUpCard1 = null;
		mFaceUpCard2 = null;
	}

	// user touched device identified by data
	protected void handleClick(final Long id, MemoryClick data) {
		final DeviceData deviceData = mDeviceData.get(id);
		if (deviceData == null)
			return;
		
		final int cardId = data.getCardId();
		final Card card = deviceData.getCard(cardId);

		if (card != null) {
			switch (card.getState()) {
			case DOWN:
				handleCardDown(deviceData, cardId, card);
				break;
			case UP:
				handleCardUp();
				break;
			default:
				break;
			}
		} else {
			System.out.println("WTF that card is not a card");
		}
	}

	private void handleCardUp() {
		if (isWin()) {
			// todo: restart game
			System.out.println("Thanks for clicking the win screen. Starting a new game...");

			// send request for ids

		} else {
			System.out.println("Card already up");
		}
	}

	private void handleCardDown(final DeviceData deviceData, final int cardId, final Card card) {
		System.out.println("Flip it up yo! deviceData: " + deviceData.getId());

		if (mFaceUpCard1 == null) {
			mFaceUpCard1 = new FaceUpCard(card, deviceData, cardId);
			card.setState(CardState.UP);

			final String queueName = deviceData.getQueueName();
			final MemoryFlip memoryFlip = new MemoryFlip(MemoryFlip.UP, cardId);
			final Long id = mFaceUpCard1.getDeviceData().getId();
			final String udid = mFaceUpCard1.getDeviceData().getMobileServerIdentifierData().getDeviceSerial();
			if (id != null)
				amqpConnectionManager.publishToBinding(Long.toString(id), MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());
			else if (udid != null)
				amqpConnectionManager.publishToBinding(udid, MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());
			else
				amqpConnectionManager.publishToBinding(queueName, MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());
		} else if (mFaceUpCard2 == null) {
			mFaceUpCard2 = new FaceUpCard(card, deviceData, cardId);
			card.setState(CardState.UP);
			final Card card1 = mFaceUpCard1.getCard();
			final Card card2 = mFaceUpCard2.getCard();

			final String queueName = deviceData.getQueueName();
			final MemoryFlip memoryFlip = new MemoryFlip(MemoryFlip.UP, cardId);
			final Long id = mFaceUpCard2.getDeviceData().getId();
			final String udid = mFaceUpCard2.getDeviceData().getMobileServerIdentifierData().getDeviceSerial();
			if (id != null)
				amqpConnectionManager.publishToBinding(Long.toString(id), MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());
			else if (udid != null)
				amqpConnectionManager.publishToBinding(udid, MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());
			else
				amqpConnectionManager.publishToBinding(queueName, MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());

			if (card1.getPicId() == card2.getPicId()) {
				System.out.println("Match!");
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}

				flipCardDown(mFaceUpCard1);
				flipCardDown(mFaceUpCard2);

				System.out.println("No match, turning cards over again.");
			}

			mFaceUpCard1 = null;
			mFaceUpCard2 = null;

		} else {
			// this should never happen
		}

		if (isWin()) {
			// send win message
			System.out.println("Win!");
			amqpConnectionManager.publishToAll(MemoryMessageType.FLIP.toString(), new MemoryFlip(MemoryFlip.WIN, new Integer(cardId)).toJson().toString());
		}
	}

	private void flipCardDown(final FaceUpCard faceUpCard) {
		final Card card = faceUpCard.getCard();
		card.setState(CardState.DOWN);
		final Integer cardId = faceUpCard.getCardId();
		final DeviceData deviceData = faceUpCard.getDeviceData();
		final String queueName = deviceData.getQueueName();
		final MemoryFlip memoryFlip = new MemoryFlip(MemoryFlip.DOWN, cardId);
		final Long id = faceUpCard.getDeviceData().getId();
		if (id != null)
			amqpConnectionManager.publishToBinding(Long.toString(id), MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());
		else
			amqpConnectionManager.publishToBinding(queueName, MemoryMessageType.FLIP.toString(), memoryFlip.toJson().toString());
	}

	protected boolean isWin() {
		for (Long id : mDeviceData.keySet()) {
			final DeviceData deviceData = mDeviceData.get(id);
			for (final Card card : deviceData.getCards())
				if (card.getState() == CardState.DOWN)
					return false;
		}

		return true;
	}

}
