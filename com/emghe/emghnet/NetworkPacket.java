package com.emghe.emghnet;

import java.net.DatagramPacket;

public class NetworkPacket {
	private DatagramPacket packet;
	private NetworkPeer peer;
	private byte[] header;
	private byte[] data;
	
	public NetworkPacket(DatagramPacket packet, NetworkPeer peer, byte[] header, byte[] data){
		this.packet = packet;
		this.peer = peer;
		this.header = header;
		this.data = data;
	}
	
	public DatagramPacket getPacket(){
		return packet;
	}
	
	public byte[] getHeader(){
		return header;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public NetworkPeer getPeer(){
		return peer;
	}
	
	/** String representation of the packet, basically a high level packet dump **/
	@Override
	public String toString(){
		String h = "";
		for(byte b : header){
			h += (int) (b & 0xFF);
			h += " ";
		}
		String d = "";
		for(byte b : data){
			d += (int) (b & 0xFF);
			d += " ";
		}
		return String.format("NetworkPacket Dump...\nPacket Type: %d\nHeader: %s\nData: %s", 
										header[NetworkProtocols.OCTAL_PKT_TYPE] & 0xFF, h, d);
	}
}
