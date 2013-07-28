package com.emghe.emghnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.emghe.emghnet.packets.PacketHello;

public class NetworkSender implements Runnable{
	private final Networker networker;
	
	public NetworkSender(Networker networker){
		this.networker = networker;
		outputData = new byte[NetworkProtocols.PACKET_SIZE + NetworkProtocols.HEADER_SIZE];//define packet size as data size + header size
		
		if(networker.getId() == NetworkProtocols.NEW_CLIENT_ID){ // send hello to server as soon as we start the client
			PacketHello hello = PacketHello.create(networker);
			this.send(PacketHello.getHeader(), hello.getData(), networker.getServer());
		}
	}
	
	private LinkedList<byte[]> toBeSent = new LinkedList<byte[]>();
	
	private byte[] outputData;
	
	private boolean running = true;
	
	public synchronized boolean isRunning(){
		return running;
	}
	
	public synchronized void stop(){
		running = false;
	}
	
	private synchronized void add(byte[] data){
		toBeSent.add(data);
	}
	
	public synchronized byte[] pop(){
		byte[] data;
		try{
			data = toBeSent.pop();
		}catch(NoSuchElementException e){
			data = null;
		}
		return data;
	}
	
	public void send(byte[] rawData, NetworkPeer peer){
		send(Networker.createEmptyHeader(), rawData, peer);
	}
	
	/** Be aware: do not exceed {@link #outputData} size (packet size) otherwise packet will be dropped! **/
	public void send(byte[] header, byte[] rawData, NetworkPeer peer){			
		if(rawData.length > outputData.length - NetworkProtocols.HEADER_SIZE){
			assert false;
		}else{
			byte[] data = new byte[outputData.length];
			for(int i = 0; i < rawData.length; i++){
				data[i + NetworkProtocols.HEADER_SIZE] = rawData[i];					
			}
			for(int i = 0; i < header.length; i++){
				if(i >= NetworkProtocols.HEADER_SIZE){
					break;
				}
				data[i] = header[i];
			}
			data[NetworkProtocols.OCTAL_PEER_ID] = peer.id;
			this.add(data);
		}
	}
	
	/** 
	 * This is where the target peer is loaded using the id found in the current packet <br> 
	 * The object is always the same, only the properties of it change. That's for (little) performance reasons.
	 **/
	NetworkPeer peer = new NetworkPeer();
	
	@Override
	public void run() {
		while(isRunning()){
			byte[] data = this.pop();
			 
			if(data != null){
				for(int i = 0; i < outputData.length; i++){
					outputData[i] = data[i];
				}
				outputData[NetworkProtocols.OCTAL_SELF_ID] = networker.getId();
				
				networker.loadPeer(outputData[NetworkProtocols.OCTAL_PEER_ID], peer);
				
				DatagramPacket sendPacket = new DatagramPacket(outputData, outputData.length, peer.address, peer.listenPort);
				
				try {
					networker.senderSocket.send(sendPacket);
				} catch (IOException e) {
					System.err.println("NetworkSender: socket exception");
					e.printStackTrace();
					System.out.println("NetworkSender: packet data: " + new String(outputData));
				}
			}
		}
		networker.senderSocket.close();
		System.out.println("NetworkSender: processor quited");
	}
}
