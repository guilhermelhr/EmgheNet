package com.emghe.emghnet;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.LinkedList;

public abstract class Networker {
	/** 
	 * NEW_CLIENT_ID is interpreted by the server as a new client waiting to receive an unique id.
	 * SERVER_ID is the server unique id.
	 **/
	protected short id = NetworkProtocols.NEW_CLIENT_ID;	
	
	public NetworkReceiver receiver;
	public NetworkSender sender;
	
	private short listenPort;
	
	protected NetworkEventListener eventListener;
	public void registerListener(NetworkEventListener listener){
		this.eventListener = listener;
	}
	
	/**
	 * Stores the peers (other networkers) known by this networker.
	 * Usually, the clients will only know about the server
	 * and the server will know about all clients.
	 */
	private LinkedList<NetworkPeer> peers;
	
	/**
	 * Creates a networker. It is made of two sockets, the {@link #receiverSocket} and the {@link #senderSocket}
	 * that are managed by {@link #receiver} and {@link #sender}.
	 * You may specify through {@link #id} the role it should take (client or server).
	 * @param listenPort
	 */
	public Networker(short listenPort){
		this.listenPort = listenPort; 
		peers = new LinkedList<NetworkPeer>();
	}
	
	/** 
	 * @param data bytes to be sent
	 * @param targetId who will it be sent to
	 * @return was it sent successfully sent to be processed (doesn't mean it was sent) 
	 */
	public boolean sendData(byte[] data, short targetId){
		return sender.send(Networker.createEmptyHeader(), data, getPeer(targetId));
	}
	
	public boolean sendData(String data, short targetId){
		try {
			return sendData(data.getBytes(NetworkProtocols.STR_ENCODING), targetId);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void packetReceived(){
		if(eventListener != null){
			NetworkPacket packet;
			while((packet = receiver.pop()) != null){
				eventListener.onPacketReceived(packet);
			}
		}
	}
	
	public void packetSent(DatagramPacket packet, byte[] rawData, NetworkPeer peer){
		if(eventListener != null){
			NetworkPacket np = new NetworkPacket(packet, peer, rawData);
			eventListener.onPacketSent(np);
		}
	}
	
	public short getFreeId(){
		short id;
		boolean found = false;
		for(id = 0; NetworkHelper.unsignShort(id) <= 65535; id++){
			if((id == NetworkProtocols.NEW_CLIENT_ID) || (id == NetworkProtocols.SERVER_ID) || (id == NetworkProtocols.INVALID_ID)){
				continue;
			}
			if(getPeer(id) == null){
				found = true;
				break;
			}
		}
		if(!found){
			System.err.println("Networker can't find an available id!");
			return NetworkProtocols.INVALID_ID;
		}
		return id;
	}
	
	public static byte[] createEmptyHeader(){
		byte[] header = new byte[NetworkProtocols.HEADER_SIZE];
		header[NetworkProtocols.OCTAL_PKT_TYPE] = NetworkProtocols.TYPE_RAW;
		return header;
	}
	
	public synchronized void addPeer(NetworkPeer peer){
		peers.add(peer);
		if(eventListener != null){
			eventListener.onConnect(peer);
		}
	}
	
	public synchronized void removePeer(NetworkPeer peer){
		peers.remove(peer);
	}
	
	public synchronized NetworkPeer getPeer(short id){
		for(NetworkPeer peer : peers){
			if(peer.id == id){
				return peer;
			}
		}
		return null;
	}
	
	public int getPeersSize(){
		return peers.size();
	}
	
	public synchronized NetworkPeer loadPeer(short id, NetworkPeer target){
		NetworkPeer peer = getPeer(id);
		if(peer != null){
			target.id = peer.id;
			target.address = peer.address;
			target.listenPort = peer.listenPort;
		}else{
			target.id = NetworkProtocols.INVALID_ID;
			target.address = null;
			target.listenPort = 0;
		}
		return peer;
	}
	
	public synchronized NetworkPeer getServer(){
		return getPeer(NetworkProtocols.SERVER_ID);
	}
	
	public short getListenPort(){
		return listenPort;
	}
	
	public short getId(){
		return id;
	}
	
	public boolean isNewClient(){
		return id == NetworkProtocols.NEW_CLIENT_ID;
	}
	
	public boolean isServer(){
		return id == NetworkProtocols.SERVER_ID;
	}
	
	public void run(){
		if(receiver != null) receiver.stop();
		receiver = new NetworkReceiver(this, listenPort);
		listenPort = (short) receiver.socket.getLocalPort();
		System.out.println("Networker: starting receiver socket on port " + NetworkHelper.unsignShort(listenPort));
		Thread tr = new Thread(receiver);
		tr.start();
		sender = new NetworkSender(this);
		System.out.println("Networker: starting sender socket on port " + sender.socket.getLocalPort());
		Thread ts = new Thread(sender);
		ts.start();
		System.out.println("Networker: network is now running");
	}
	
}
