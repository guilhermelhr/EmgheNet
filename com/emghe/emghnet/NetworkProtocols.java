package com.emghe.emghnet;

public class NetworkProtocols {
	
	public static final byte
	/** packet octals **/
	OCTAL_SELF_ID 	= 0,
	OCTAL_PEER_ID 	= 1,
	OCTAL_PKT_TYPE	= 2,
	/*******************/
	
	HEADER_SIZE 		= 3,
	
	/** packet types **/
	TYPE_RAW			= 0,
	TYPE_HELLO 			= 1,
	/******************/
	
	/** ids **/
	SERVER_ID 			= (byte) 255,
	NEW_CLIENT_ID 		= (byte) 254;
	/*********/
}
