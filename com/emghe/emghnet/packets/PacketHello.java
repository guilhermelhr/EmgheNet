package com.emghe.emghnet.packets;

import java.net.DatagramPacket;

import com.emghe.emghnet.NetworkPeer;
import com.emghe.emghnet.NetworkProtocols;
import com.emghe.emghnet.Networker;

public class PacketHello {
	
	private static final byte ID = 0, LISTEN_PORT = 1;
	
	public NetworkPeer me; 
	
	public static PacketHello load(byte[] data, DatagramPacket packet){
		PacketHello hello = new PacketHello();
		hello.me = new NetworkPeer();
		hello.me.id = data[ID];
		hello.me.address = packet.getAddress();
		int listenPort = 0;
		listenPort |= data[LISTEN_PORT] & 0xFF;
		listenPort |= (data[LISTEN_PORT + 1] & 0xFF) << 8;
		listenPort |= (data[LISTEN_PORT + 2] & 0xFF) << 16;
		listenPort |= (data[LISTEN_PORT + 3] & 0xFF) << 24;
		hello.me.listenPort = listenPort;
		return hello;
	}
	
	public static PacketHello create(Networker networker){
		PacketHello hello = new PacketHello();
		hello.me = new NetworkPeer();
		hello.me.id = networker.getId();
		hello.me.listenPort = networker.getListenPort();
		return hello;
	}
	
	public static byte[] getHeader(){
		byte[] header = Networker.createEmptyHeader();
		header[NetworkProtocols.OCTAL_PKT_TYPE] = NetworkProtocols.TYPE_HELLO;
		return header;
	}
	
	public byte[] getData(){
		byte[] hello = new byte[5];
		hello[ID] = me.id;
		hello[LISTEN_PORT] = (byte) (me.listenPort & 0xFF);
		hello[LISTEN_PORT + 1] = (byte) ((me.listenPort >> 8)  & 0xFF);
		hello[LISTEN_PORT + 2] = (byte) ((me.listenPort >> 16) & 0xFF);
		hello[LISTEN_PORT + 3] = (byte) ((me.listenPort >> 24) & 0xFF);
		return hello;
	}
	
}
