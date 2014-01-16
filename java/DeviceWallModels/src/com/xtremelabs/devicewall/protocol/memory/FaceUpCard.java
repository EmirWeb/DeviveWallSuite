package com.xtremelabs.devicewall.protocol.memory;


public class FaceUpCard {
	private final Card mCard;
	private final DeviceData mDeviceData;
	private final Integer mCardId;

	public FaceUpCard(Card card, DeviceData deviceData, Integer cardId) {
		super();
		mCard = card;
		mDeviceData = deviceData;
		mCardId = cardId;
	}

	public Card getCard() {
		return mCard;
	}

	public DeviceData getDeviceData() {
		return mDeviceData;
	}

	public Integer getCardId() {
		return mCardId;
	}
}
