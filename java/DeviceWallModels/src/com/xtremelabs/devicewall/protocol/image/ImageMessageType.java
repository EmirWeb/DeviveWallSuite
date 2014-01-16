package com.xtremelabs.devicewall.protocol.image;

public enum ImageMessageType {
	DATA("image_data"), CONFIRM("image_confirm"), EMPTY("");

	private String type;

	private ImageMessageType(String type) {
		this.type = type;
	}

	public static ImageMessageType getModelType(final String name) {
		for (ImageMessageType modelType : ImageMessageType.values())
			if (modelType.type.equals(name))
				return modelType;

		return ImageMessageType.EMPTY;
	}

	public String toString() {
		return type;
	}
}
