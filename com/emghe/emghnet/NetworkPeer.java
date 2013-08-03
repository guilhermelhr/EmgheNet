package com.emghe.emghnet;

import java.net.InetAddress;

public class NetworkPeer {
	public byte id;
	public InetAddress address;
	public int listenPort;
	public int packetSize;
	
	@Override
	public String toString(){
		return String.format("NetworkPeer{id: %d, address: %s, port: %d, packet size: %d", 
											id & 0xFF, address.getHostAddress(), listenPort, packetSize);
	}
}
