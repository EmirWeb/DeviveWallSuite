package com.xtremelabs.devicewall.protocol.identifier;

public enum IdentifierMessageType {
	MOBILE_SERVER_IDENTIFIER_REQUEST("mobile_server_identifier_request"), DESKTOP_SERVER_IDENTIFIER_REQUEST("desktop_server_identifier_request"), SERVER_IDENTIFIER_RESPONSE("server_identifier_response"), MAP_SERVER_REQUEST("map_server_request"), MAP_SERVER_RESPONSE("map_server_response"), EMPTY("");

	private String type;

	private IdentifierMessageType(String type) {
		this.type = type;
	}

	public static IdentifierMessageType getModelType(final String name) {
		for (IdentifierMessageType modelType : IdentifierMessageType.values())
			if (modelType.type.equals(name))
				return modelType;

		return IdentifierMessageType.EMPTY;
	}

	@Override
	public String toString() {
		return type;
	}
}