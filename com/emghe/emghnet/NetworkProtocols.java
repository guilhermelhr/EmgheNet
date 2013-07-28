package com.emghe.emghnet;

public class NetworkProtocols {
	
	/**
	 * <p>The packet size is the size in bytes of all packets that are sent
	 * by the networkers. You should change this to be the
	 * minimum required by the data you want to send. <br>
	 * Right now, each packet has {@value} bytes. </p>
	 * <p>It's important to notice that even if you send less data
	 * than the size of the packet, the networker will fill what is left with 0s. <br>
	 * If you exceed the specified size, the packet will be dropped and
	 * there will be an assert notification. </p>
	 */
	public static final int PACKET_SIZE = 128;
	
	/**
	 * Pretty much the same as {@link #PACKET_SIZE}, but for the header. <br>
	 * The real size of the packet will be {@link #HEADER_SIZE} + {@link #PACKET_SIZE}
	 */
	public static final int HEADER_SIZE = 3;
	
	public static final byte
	/** packet octals **/
	OCTAL_SELF_ID 	= 0,
	OCTAL_PEER_ID 	= 1,
	OCTAL_PKT_TYPE	= 2,
	/*******************/
	
	/** packet types **/
	TYPE_RAW			= 0,
	TYPE_HELLO 			= 1,
	/******************/
	
	/** ids **/
	SERVER_ID 			= (byte) 255,
	NEW_CLIENT_ID 		= (byte) 254;
	/*********/
}
