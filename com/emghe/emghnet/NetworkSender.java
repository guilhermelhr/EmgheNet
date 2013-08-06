package com.emghe.emghnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
		
		outputData = ByteBuffer.allocate(NetworkProtocols.PACKET_TOTAL_SIZE);//define packet size as data size + header size
		
		if(networker.isNewClient()){ // send hello to server as soon as we start the client
			PacketHello hello = PacketHello.create(networker);
			this.send(PacketHello.getHeader(), hello.getData(), networker.getServer());
		}
	}
	
	public ThroughputMeter getThroughputMeter(){
		return meter;
	}
	
	private LinkedList<byte[]> toBeSent = new LinkedList<byte[]>();
	
	private ByteBuffer outputData;
	
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
	
	public boolean send(byte[] rawData, NetworkPeer peer){
		return send(Networker.createEmptyHeader(), rawData, peer);
	}
	
	/** Be aware: do not exceed {@link #outputData} size (packet size) otherwise packet will be dropped! **/
	public boolean send(byte[] header, byte[] data, NetworkPeer peer){
		String dropReason = null;
		if(data.length > outputData.capacity() - NetworkProtocols.HEADER_SIZE) 	dropReason = "data buffer overflow";
		if(peer.id == NetworkProtocols.INVALID_ID) 							dropReason = "invalid peer id";
		if(header.length > NetworkProtocols.HEADER_SIZE) 					dropReason = "header buffer overflow";
		
		if(dropReason != null){
			System.err.println("NetworkSender: A packet was dropped due to " + dropReason);
			assert false;
		}else if(peer != null){
			byte[] fHeader = new byte[NetworkProtocols.HEADER_SIZE];
			for(int i = 0; i < fHeader.length; i++){
				fHeader[i] = ((i < header.length)? header[i] : 0);
			}
			
			ByteBuffer buffer = ByteBuffer.allocate(outputData.capacity());
			buffer.put(header);
			buffer.put(data);
			buffer.putShort(NetworkProtocols.OCTAL_PEER_ID, peer.id);
			this.add(buffer.array());
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
				outputData.clear();
				outputData.put(data);
				outputData.putShort(NetworkProtocols.OCTAL_SELF_ID, networker.getId());
				
				networker.loadPeer(outputData.getShort(NetworkProtocols.OCTAL_PEER_ID), peer);
				DatagramPacket sentPacket = null;
				try{
					sentPacket = new DatagramPacket(outputData.array(), outputData.capacity(),
													peer.address, NetworkHelper.unsignShort(peer.listenPort));
				}catch(IllegalArgumentException ex){
					System.out.println(peer.listenPort);
					System.exit(-1);
					return;
				}
				
				try {
					socket.send(sentPacket);
					meter.packetProcessed(outputData.capacity());
					networker.packetSent(sentPacket, outputData.array(), peer);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("NetworkSender: socket exception");
					System.out.println("NetworkSender: packet destination: " + peer.toString());
					System.out.println("NetworkSender: packet data: " + 
									new String(outputData.array(), Charset.forName(NetworkProtocols.STR_ENCODING)));
				}
			}else{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {}
			}
		}
		socket.close();
		System.out.println("NetworkSender: processor quited");
	}
}
