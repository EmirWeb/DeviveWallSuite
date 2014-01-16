package com.xtremelabs.devicewall.protocol.memory;

import java.util.ArrayList;

import com.xtremelabs.devicewall.protocol.identifier.data.MobileServerIdentifierData;

public class DeviceData {

	final ArrayList<Card> mCards;
	final MobileServerIdentifierData mMobileServerIdentifierData ;

	public DeviceData(final MobileServerIdentifierData mobileServerIdentifierData, final ArrayList<Card> cards) {
		if (cards == null)
			mCards = new ArrayList<Card>();
		else
			mCards = cards;
		
		mMobileServerIdentifierData = mobileServerIdentifierData;
	}

	public ArrayList<Card> getCards() {
		return mCards;
	}

	public Card getCard(final int cardIndex) {
		if (cardIndex < 0 || cardIndex >= mCards.size())
			return null;
		return mCards.get(cardIndex);
	}
	
	public MobileServerIdentifierData getMobileServerIdentifierData(){
		return mMobileServerIdentifierData;
	}
	
	public long getId(){
		if (mMobileServerIdentifierData == null)
			return -1;
		return mMobileServerIdentifierData.getId();
	}

	public String getQueueName() {
		if (mMobileServerIdentifierData == null)
			return null;
		return mMobileServerIdentifierData.getQueueName();
	}
}
