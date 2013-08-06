package com.emghe.emghnet;

import java.net.InetAddress;

public class NetworkPeer {
	public short id;
	public InetAddress address;
	public short listenPort;
	public int packetSize;
	public int ping;
	
	@Override
	public String toString(){
		return String.format("NetworkPeer{id: %d, address: %s, port: %d, packet size: %d}", 
											NetworkHelper.unsignShort(id), address.getHostAddress(), 
												NetworkHelper.unsignShort(listenPort), packetSize, ping);
	}
}
