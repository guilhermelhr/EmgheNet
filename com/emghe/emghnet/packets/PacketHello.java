package com.emghe.emghnet.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.emghe.emghnet.NetworkHelper;
import com.emghe.emghnet.NetworkProtocols;
import com.emghe.emghnet.Networker;

public class PacketHello {
	
	public InetAddress address;
	public short id;
	public short listenPort;
	
	public static PacketHello create(Networker networker){
		PacketHello hello = new PacketHello();
		hello.id = networker.getId();
		hello.listenPort = networker.getListenPort();
		return hello;
	}
	
	public static PacketHello read(byte[] data, DatagramPacket packet){
		PacketHello hello = new PacketHello();
		hello.address = packet.getAddress();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		hello.id = buffer.getShort();
		hello.listenPort = buffer.getShort();
		return hello;
	}
	
	public static byte[] getHeader(){
		byte[] header = Networker.createEmptyHeader();
		header[NetworkProtocols.OCTAL_PKT_TYPE] = NetworkProtocols.TYPE_HELLO;
		return header;
	}
	
	public byte[] getData(){
		int bufferSize = NetworkHelper.getSizeOfNum(id) + NetworkHelper.getSizeOfNum(listenPort);
		ByteBuffer hello = ByteBuffer.allocate(bufferSize); 
		hello.putShort(id);
		hello.putShort(listenPort);
		
		return hello.array();
	}
	
}
