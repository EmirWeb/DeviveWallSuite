package com.xtremelabs.devicewall.protocol.memory;

public enum MemoryMessageType {
	FLIP("memory_flip"), CLICK("memory_click"), ASSIGN("memory_assign"), CONFIRM("memory_confirm"), EMPTY("");
	
	private String type;
	
	private MemoryMessageType(String type) {
		this.type = type;
	}
	
	public static MemoryMessageType getModelType(final String name) {
		for (MemoryMessageType modelType : MemoryMessageType.values())
			if (modelType.type.equals(name))
				return modelType;

		return MemoryMessageType.EMPTY;
	}
	
	public String toString() {
		return type;
	}
}
