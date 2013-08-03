package com.emghe.emghnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.emghe.emghnet.packets.PacketHello;

public class NetworkSender implements Runnable{
	
	private final Networker networker;
	protected DatagramSocket socket;
	private ThroughputMeter meter;
		
	public NetworkSender(Networker networker){
		this.networker = networker;
		meter = new ThroughputMeter();
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.println("Could not start sender socket");
			e.printStackTrace();
		}
		
		outputData = new byte[NetworkProtocols.PACKET_TOTAL_SIZE];//define packet size as data size + header size
		
		if(networker.isNewClient()){ // send hello to server as soon as we start the client
			PacketHello hello = PacketHello.create(networker);
			this.send(PacketHello.getHeader(), hello.getData(), networker.getServer());
		}
	}
	
	public ThroughputMeter getThroughputMeter(){
		return meter;
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
	public boolean send(byte[] header, byte[] rawData, NetworkPeer peer){			
		if(rawData.length > outputData.length - NetworkProtocols.HEADER_SIZE){
			assert false;
		}else if(peer != null){
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
			return true;
		}
		return false;
	}
	
	/** 
	 * This is where the target peer is loaded using the id found in the current packet <br> 
	 * The object is always the same, only the properties of it change. That's for (little) performance reasons.
	 **/
	NetworkPeer peer = new NetworkPeer();
	
	@Override
	public void run() {
		while(isRunning()){
			meter.tick();
			
			byte[] data = this.pop();
			 
			if(data != null){
				for(int i = 0; i < outputData.length; i++){
					outputData[i] = data[i];
				}
				outputData[NetworkProtocols.OCTAL_SELF_ID] = networker.getId();
				
				networker.loadPeer(outputData[NetworkProtocols.OCTAL_PEER_ID], peer);
				DatagramPacket sentPacket = null;
				try{
					sentPacket = new DatagramPacket(outputData, outputData.length, peer.address, peer.listenPort);
				}catch(IllegalArgumentException ex){
					System.out.println(peer.listenPort);
					System.exit(-1);
					return;
				}
				
				try {
					socket.send(sentPacket);
					meter.packetProcessed(outputData.length);
					networker.packetSent(sentPacket, outputData, peer);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("NetworkSender: socket exception");
					System.out.println("NetworkSender: packet destination: " + peer.toString());
					System.out.println("NetworkSender: packet data: " + new String(outputData));
				}
			}
		}
		socket.close();
		System.out.println("NetworkSender: processor quited");
	}
}
