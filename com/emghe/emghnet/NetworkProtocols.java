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
	public static final int PACKET_SIZE = 32;
	
	/**
	 * Pretty much the same as {@link #PACKET_SIZE}, but for the header. <br>
	 * The real size of the packet will be {@link #HEADER_SIZE} + {@link #PACKET_SIZE}
	 */
	public static final int HEADER_SIZE = 5;
	
	public static final int PACKET_TOTAL_SIZE = PACKET_SIZE + HEADER_SIZE;
	
	/**
	 * Encoding used for String to byte[] conversion
	 */
	public static final String STR_ENCODING = "UTF-8";
	
	public static final byte
	/** packet octals **/
	OCTAL_SELF_ID 	= 0, // 2 Bytes
	OCTAL_PEER_ID 	= 2, // 2 Bytes
	OCTAL_PKT_TYPE	= 4, // 1 Byte
	/*******************/
	
	/** packet types **/
	TYPE_RAW			= 0,
	TYPE_HELLO 			= 1;
	/******************/

	public static final short
	/** ids **/
	INVALID_ID			= (short) 253,
	SERVER_ID 			= (short) 255,
	NEW_CLIENT_ID 		= (short) 254;	
	/*********/
}
