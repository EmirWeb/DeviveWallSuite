package com.xtremelabs.devicewall.protocol.gamecontrol;

public enum GameControlMessageType {
	CLIENT_START("client_start"), SERVER_START("server_start"), EMPTY("");

	private String type;

	private GameControlMessageType(String type) {
		this.type = type;
	}

	public static GameControlMessageType getModelType(final String name) {
		for (GameControlMessageType modelType : GameControlMessageType.values())
			if (modelType.type.equals(name))
				return modelType;

		return GameControlMessageType.EMPTY;
	}

	@Override
	public String toString() {
		return type;
	}
}
