package com.xtremelabs.devicewall.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xtremelabs.devicewall.protocol.identifier.IdentifierDeserializer;

public class AmqpConstants {
	public static final String ANDRES_COMPUTER_ON_THE_FIFTH = "192.168.91.179";
	public static final String PETERS_LAPTOP = "192.168.100.110";
	public static final String EMIRS_DESKTOP = "192.168.91.208";
	public static final String EMIRS_LAPTOP = "192.168.101.171";
	public static final String LOCALHOST = "localhost";
	public static final String DEVIN_TOM = "192.168.90.244";
	public static final String MOES_PC = "192.168.101.250";
	public static final String DEVINS_MACBOOK = "192.168.100.76";
	public static final String PETERS_LAPTOP2 = "10.0.0.111";
	public static final String PETERS_LAPTOP4 = "192.168.100.110";
	public static final String PETERS_LAPTOP3 = "192.168.0.100";
	public static final String PETERS_LAPTOP5 = "192.168.0.117";
	public static final String PETERS_LAPTOP6 = "192.168.1.5";
	public static final String DESKTOP_HOST = LOCALHOST;
	public static final String MOBILE_HOST = PETERS_LAPTOP6;
	
	
	public static final int PORT = 5672;
	
	public static final String SERVER_EXCHANGE = "device_wall_identifier_exchange";
	public static final String SERVER_ROUTING_KEY = "device_wall_identifier_routing_key";
	public static final String CLIENT_ROUTING_KEY = "device_wall_identifier_client_routing_key";
	public static final String SERVER_QUEUE_NAME = "device_wall_identifier_queue";
	
	
	public static final String MEMORY_SERVER_APP_NAME = "memory";
	public static final String IMAGE_SERVER_APP_NAME = "image";
	public static final String IDENTIFIER_SERVER_APP_NAME = "identifier";
	
	public static final Gson sIdentifierGson;
	static {
		final GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Protocol.class, new IdentifierDeserializer());
		sIdentifierGson = builder.create();
	}
	
}
