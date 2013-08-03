package com.emghe.emghnet;

public class ThroughputMeter {
	private float bps = 0f;
	private int currBits = 0;
	private int totalBits = 0;
	private double lastCalc = System.currentTimeMillis();
	private int refreshRate = 4000;
	
	public void tick(){
		float delta = (float) (System.currentTimeMillis() - lastCalc);
		if(delta >= refreshRate){
			bps = currBits / (delta / 1000f);
			
			lastCalc = System.currentTimeMillis();
			currBits = 0;
		}
	}
	
	/**
	 * Notifies meter that a packet was processed
	 * @param size Packet size in bytes
	 */
	public void packetProcessed(int size){
		currBits += size * 8;
		totalBits += size * 8;
	}
	
	public int getTotalBits(){
		return totalBits;
	}
	
	public float getBitsPerSecond(){
		return bps;
	}
	
	public float getBytesPerSecond(){
		return bps / 8f;
	}
	
	public float getKbps(){
		return bps / 1000f;
	}
	
	public float getMbps(){
		return bps / 1000000f;
	}
	
	public float getKBps(){
		return (bps / 8f) / 1000f;
	}
	
	public float getMBps(){
		return (bps / 8f) / 1000000f;
	}
}
