package com.emghe.emghnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.emghe.emghnet.packets.PacketHello;

public class NetworkReceiver implements Runnable {

	private final Networker networker;
	protected DatagramSocket socket;
	private ThroughputMeter meter;
	
	NetworkReceiver(Networker networker, int listenPort) {
		this.networker = networker;
		meter = new ThroughputMeter();
		try {
			if(listenPort == 0){
				socket = new DatagramSocket();
			}else{
				socket = new DatagramSocket(listenPort);
			}
		} catch (SocketException e) {
			System.err.println(String.format("Could not start receiver socket on port %d", listenPort));
			e.printStackTrace();
		}
		inputData = new byte[NetworkProtocols.PACKET_TOTAL_SIZE];
	}
	
	public ThroughputMeter getThroughputMeter(){
		return meter;
	}

	/** packet buffer **/
	private LinkedList<DatagramPacket> packets = new LinkedList<DatagramPacket>();
	
	private byte[] inputData;
			
	private boolean running = true;
	
	public synchronized boolean isRunning(){
		return running;
	}
	
	public synchronized void stop(){
		running = false;
	}
	
	/**
	 * Gets the first packet on the buffer.<br>
	 * <b>Do not call this directly</b> if you're using an event listener on your networker. It already does that for you.
	 * @return packet or null if buffer is empty or the packet is for internal communication.
	 */
	public synchronized NetworkPacket pop(){
		DatagramPacket packet;
		try{
			packet = packets.pop();
		}catch(NoSuchElementException e){
			return null;
		}
		
		byte[] rawData = packet.getData();
		ByteBuffer header = ByteBuffer.allocate(NetworkProtocols.HEADER_SIZE);
		header.put(NetworkPacket.swapIds(NetworkPacket.readHeader(rawData)));
		byte[] data = NetworkPacket.removeHeader(rawData);
				
		byte packetType = header.get(NetworkProtocols.OCTAL_PKT_TYPE);
		NetworkPeer peer = networker.getPeer(header.getShort(NetworkProtocols.OCTAL_PEER_ID));
		
		// if the packet is relevant to the internal network system, read it.
		// packets that are created by the user (type_raw) should be send directly to the next layer.
		if(packetType != NetworkProtocols.TYPE_RAW){
			if(networker.getId() == NetworkProtocols.SERVER_ID){
				readAsServer(packetType, rawData, header.array(), data, packet, peer);
			}else{
				readAsClient(packetType, rawData, header.array(), data, packet, peer);
			}
			commonRead(packetType, rawData, header.array(), data, packet, peer);
			return null;
		}
		
		NetworkPacket np = new NetworkPacket(packet, peer, header.array(), data);
		return np;
	}
	
	private synchronized void add(DatagramPacket packet){
		packets.addLast(packet);
	}
	
	public void process(){
		try {
			DatagramPacket received = new DatagramPacket(inputData, inputData.length);
			
			socket.receive(received);
						
			meter.packetProcessed(inputData.length);
			
			this.add(received);
			
			networker.packetReceived();
	
		} catch (IOException e) {
			System.err.println("NetworkReceiver: socket exception");
			e.printStackTrace();
			System.out.println("NetworkReceiver: last packet data: " + new String(inputData));
		}
	}
	
	@Override
	public void run() {
		while(isRunning()){
			meter.tick();
			process();			
		}
		socket.close();
		System.out.println("NetworkReceiver: processor quited");
	}
	
	private void commonRead(byte packetType, byte[] rawData, byte[] header, byte[] data, DatagramPacket packet, NetworkPeer peer){
		switch (packetType) {
		
		}
	}
	
	private void readAsServer(byte packetType, byte[] rawData, byte[] header, byte[] data, DatagramPacket packet, NetworkPeer peer){
		switch(packetType){
			case NetworkProtocols.TYPE_HELLO:
				PacketHello ph = PacketHello.read(data, packet);
				peer = new NetworkPeer();
				ph.id = peer.id = networker.getFreeId();
				peer.address = packet.getAddress();
				peer.listenPort = ph.listenPort;
				networker.addPeer(peer);
				networker.sender.send(PacketHello.getHeader(), ph.getData(), peer);
				break;
		}
	}
	
	private void readAsClient(byte packetType, byte[] rawData, byte[] header, byte[] data, DatagramPacket packet, NetworkPeer peer){
		switch(packetType){
			case NetworkProtocols.TYPE_HELLO:
				PacketHello ph = PacketHello.read(data, packet);
				networker.id = ph.id;
				if(networker.eventListener != null) networker.eventListener.onConnect(peer);
				break;
		}
	}
}