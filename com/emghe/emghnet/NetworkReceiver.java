package com.emghe.emghnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.emghe.emghnet.packets.PacketHello;

public class NetworkReceiver implements Runnable {

	private final Networker networker;
	
	NetworkReceiver(Networker networker) {
		this.networker = networker;
		inputData = new byte[NetworkProtocols.PACKET_SIZE + NetworkProtocols.HEADER_SIZE];
	}

	/** packet buffer **/
	private LinkedList<DatagramPacket> packets = new LinkedList<DatagramPacket>();
	
	private byte[] inputData;
			
	private boolean running = true;
	
	private int packetsReceived = 0;	
	private double lastCalc = System.currentTimeMillis();
	private float bps = 0f;
	
	public synchronized boolean isRunning(){
		return running;
	}
	
	public synchronized void stop(){
		running = false;
	}
	
	public synchronized NetworkPacket pop(){
		DatagramPacket packet;
		try{
			packet = packets.pop();
		}catch(NoSuchElementException e){
			return null;
		}
		byte[] rawData = packet.getData();
		
		byte[] header = new byte[NetworkProtocols.HEADER_SIZE];
		for(int i = 0; i < header.length; i++){
			header[i] = rawData[i];
		}
		
		byte[] data = new byte[rawData.length - NetworkProtocols.HEADER_SIZE];
		for(int i = 0; i < data.length; i++){
			data[i] = rawData[i + NetworkProtocols.HEADER_SIZE];
		}
		
		byte packetType = header[NetworkProtocols.OCTAL_PKT_TYPE];
		NetworkPeer peer = networker.getPeer(header[NetworkProtocols.OCTAL_PEER_ID]);
		
		// if the packet is relevant to the internal network system, read it.
		// packets that are created by the user (type_raw) should be send directly to the next layer.
		if(packetType != NetworkProtocols.TYPE_RAW){
			if(networker.getId() == NetworkProtocols.SERVER_ID){
				readAsServer(packetType, rawData, header, data, packet, peer);
			}else{
				readAsClient(packetType, rawData, header, data, packet, peer);
			}
		}
		
		NetworkPacket np = new NetworkPacket(packet, peer, header, data);
		return np;
	}
	
	private synchronized void add(DatagramPacket packet){
		packets.addLast(packet);
	}
	
	public void process(){
		try {
			DatagramPacket received = new DatagramPacket(inputData, inputData.length);
			networker.receiverSocket.receive(received);
			
			packetsReceived++;
			
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
			calculateSpeed();
			process();			
		}
		networker.receiverSocket.close();
		System.out.println("NetworkReceiver: processor quited");
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
				break;
		}
	}
	
	public float getBps(){
		calculateSpeed();
		return bps;
	}
	
	private void calculateSpeed(){
		float delta = (float) (System.currentTimeMillis() - lastCalc);
		if(delta >= 1000f){
			bps = (packetsReceived* inputData.length) / (delta / 1000f);
			
			lastCalc = System.currentTimeMillis();
			packetsReceived = 0;
		}
	}
}