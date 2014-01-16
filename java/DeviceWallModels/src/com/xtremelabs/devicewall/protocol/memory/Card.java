package com.xtremelabs.devicewall.protocol.memory;

import com.xtremelabs.devicewall.protocol.CardFace;

public class Card {
	
	public enum CardState {
		DOWN,
		UP
	}

	private CardState state = CardState.DOWN;
	private CardFace picId;
	
	public Card(CardState state, CardFace picId) {
		this.state = state;
		this.picId = picId;
	}

	public CardState getState() {
		return state;
	}
	
	public void setState(CardState state) {
		this.state = state;
	}

	public CardFace getPicId() {
		return picId;
	}

	public void setPicId(CardFace picId) {
		this.picId = picId;
	}
}
