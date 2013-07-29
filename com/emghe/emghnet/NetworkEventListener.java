package com.emghe.emghnet;

public interface NetworkEventListener {
	public void onPacketReceived(NetworkPacket packet);
	public void onPacketSent(NetworkPacket packet);
	public void onConnect(NetworkPeer peer);
	public void onDisconnect(int reason);
}
