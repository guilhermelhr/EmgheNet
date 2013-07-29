package com.emghe.emghnet.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

import com.emghe.emghnet.NetworkProtocols;
import com.emghe.emghnet.Networker;

public class PacketHello {
	
	private static final byte OCTAL_ID = 0, OCTAL_LISTEN_PORT = 1;
	
	public InetAddress address;
	public byte id;
	public int listenPort;
	
	public static PacketHello create(Networker networker){
		PacketHello hello = new PacketHello();
		hello.id = networker.getId();
		hello.listenPort = networker.getListenPort();
		return hello;
	}
	
	public static PacketHello read(byte[] data, DatagramPacket packet){
		PacketHello hello = new PacketHello();
		hello.id = data[OCTAL_ID];
		hello.address = packet.getAddress();
		int listenPort = 0;
		listenPort |= data[OCTAL_LISTEN_PORT] & 0xFF;
		listenPort |= (data[OCTAL_LISTEN_PORT + 1] & 0xFF) << 8;
		listenPort |= (data[OCTAL_LISTEN_PORT + 2] & 0xFF) << 16;
		listenPort |= (data[OCTAL_LISTEN_PORT + 3] & 0xFF) << 24;
		hello.listenPort = listenPort;
		return hello;
	}
	
	public static byte[] getHeader(){
		byte[] header = Networker.createEmptyHeader();
		header[NetworkProtocols.OCTAL_PKT_TYPE] = NetworkProtocols.TYPE_HELLO;
		return header;
	}
	
	public byte[] getData(){
		byte[] hello = new byte[5];
		hello[OCTAL_ID] = id;
		hello[OCTAL_LISTEN_PORT] = (byte) (listenPort & 0xFF);
		hello[OCTAL_LISTEN_PORT + 1] = (byte) ((listenPort >> 8)  & 0xFF);
		hello[OCTAL_LISTEN_PORT + 2] = (byte) ((listenPort >> 16) & 0xFF);
		hello[OCTAL_LISTEN_PORT + 3] = (byte) ((listenPort >> 24) & 0xFF);
		return hello;
	}
	
}
