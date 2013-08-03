package com.emghe.emghnet;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.LinkedList;

public abstract class Networker {
	/** 
	 * NEW_CLIENT_ID is interpreted by the server as a new client waiting to receive an unique id.
	 * SERVER_ID is the server unique id.
	 **/
	protected byte id = NetworkProtocols.NEW_CLIENT_ID;	
	
	public NetworkReceiver receiver;
	public NetworkSender sender;
	
	private int listenPort;
	
	protected NetworkEventListener listener;
	public void registerListener(NetworkEventListener listener){
		this.listener = listener;
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
	public Networker(int listenPort){
		this.listenPort = listenPort; 
		peers = new LinkedList<NetworkPeer>();
	}
	
	/** 
	 * @param data bytes to be sent
	 * @param targetId who will it be sent to
	 * @return was it sent successfully sent to be processed (doesn't mean it was sent) 
	 */
	public boolean sendData(byte[] data, byte targetId){
		return sender.send(Networker.createEmptyHeader(), data, getPeer(targetId));
	}
	
	public boolean sendData(String data, byte targetId){
		try {
			return sendData(data.getBytes(NetworkProtocols.STR_ENCODING), targetId);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void packetReceived(){
		if(listener != null){
			NetworkPacket packet;
			while((packet = receiver.pop()) != null){
				listener.onPacketReceived(packet);
			}
		}
	}
	
	public void packetSent(DatagramPacket packet, byte[] rawData, NetworkPeer peer){
		if(listener != null){
			NetworkPacket np = new NetworkPacket(packet, peer, rawData);
			listener.onPacketSent(np);
		}
	}
	
	public byte getFreeId(){
		int id;
		for(id = 0; id < 253; id++){
			if(getPeer((byte) (id)) == null){
				break;
			}
		}
		return (byte) (id);
	}
	
	public static byte[] createEmptyHeader(){
		byte[] header = new byte[NetworkProtocols.HEADER_SIZE];
		header[NetworkProtocols.OCTAL_PKT_TYPE] = NetworkProtocols.TYPE_RAW;
		return header;
	}
	
	public synchronized void addPeer(NetworkPeer peer){
		peers.add(peer);
		if(listener != null){
			listener.onConnect(peer);
		}
	}
	
	public synchronized void removePeer(NetworkPeer peer){
		peers.remove(peer);
	}
	
	public synchronized NetworkPeer getPeer(byte id){
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
	
	public synchronized NetworkPeer loadPeer(byte id, NetworkPeer target){
		NetworkPeer peer = getPeer(id);
		if(peer != null){
			target.id = peer.id;
			target.address = peer.address;
			target.listenPort = peer.listenPort;
		}else{
			target.id = -1;
			target.address = null;
			target.listenPort = 0;
		}
		return peer;
	}
	
	public synchronized NetworkPeer getServer(){
		return getPeer(NetworkProtocols.SERVER_ID);
	}
	
	public int getListenPort(){
		return listenPort;
	}
	
	public byte getId(){
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
		listenPort = receiver.socket.getLocalPort();
		System.out.println("Networker: starting receiver socket on port " + listenPort);
		Thread tr = new Thread(receiver);
		tr.start();
		sender = new NetworkSender(this);
		System.out.println("Networker: starting sender socket on port " + sender.socket.getLocalPort());
		Thread ts = new Thread(sender);
		ts.start();
		System.out.println("Networker: network is now running");
	}
	
}
