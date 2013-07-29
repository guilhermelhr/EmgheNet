package com.emghe.emghnet;

import java.io.UnsupportedEncodingException;
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
	
	public NetworkPacket(DatagramPacket packet, NetworkPeer peer, byte[] rawData){
		this.packet = packet;
		this.peer = peer;
		header = new byte[NetworkProtocols.HEADER_SIZE];
		for(int i = 0; i < header.length; i++){
			header[i] = rawData[i];
		}
		data = new byte[rawData.length - header.length];
		for(int i = 0; i < data.length; i++){
			data[i] = rawData[i + header.length];
		}
	}
	
	public DatagramPacket getPacket(){
		return packet;
	}
	
	public byte[] getHeader(){
		return header;
	}
	
	public byte getType(){
		return header[NetworkProtocols.OCTAL_PKT_TYPE];
	}
	
	public byte[] getData(){
		return data;
	}
	
	@Override
	public String toString(){
		try {
			return new String(data, NetworkProtocols.STR_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public NetworkPeer getPeer(){
		return peer;
	}
	
	/** String representation of the packet, basically a high level packet dump **/
	public String dump(){
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
		return String.format("NetworkPacket Dump...\nPacket Type: %d\nHeader: %s\nData: %s\n%s: %s", 
										header[NetworkProtocols.OCTAL_PKT_TYPE] & 0xFF, h, d, NetworkProtocols.STR_ENCODING,this.toString());
	}
}
