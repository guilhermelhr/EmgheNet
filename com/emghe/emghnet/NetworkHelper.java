package com.emghe.emghnet;

import java.nio.ByteBuffer;

public class NetworkHelper {
	
	public static final byte SIZE_OF_BOOLEAN = 1;
	public static final byte SIZE_OF_BYTE = 1;
	public static final byte SIZE_OF_SHORT = 2;
	public static final byte SIZE_OF_INTEGER = 4;
	public static final byte SIZE_OF_LONG = 8;
	public static final byte SIZE_OF_FLOAT = 4;
	public static final byte SIZE_OF_DOUBLE = 8;
	public static final byte SIZE_OF_CHARACTER = 2;
	
	public static <T extends Object> T bytesToNum(byte[] b, Class<T> type){
		byte[] num = new byte[b.length - 1];
		for(int i = 0; i < num.length; i++){
			num[i] = b[i + 1];
		}
		
		switch(type.getSimpleName()){
			case "Boolean":
				return ((b[1] == 0)? type.cast(false): type.cast(true)); 
			case "Byte":
				return type.cast(b[1]);
			case "Short":
				return type.cast(ByteBuffer.wrap(num).asShortBuffer().get());
			case "Integer":
				return type.cast(ByteBuffer.wrap(num).asIntBuffer().get());
			case "Long":
				return type.cast(ByteBuffer.wrap(num).asLongBuffer().get());
			case "Float":
				return type.cast(ByteBuffer.wrap(num).asFloatBuffer().get());
			case "Double":
				return type.cast(ByteBuffer.wrap(num).asDoubleBuffer().get());
			case "Character":
				return type.cast(ByteBuffer.wrap(num).asCharBuffer().get());
			default:
				return null;
		}
	}
	
	public static byte[] NumToBytes(Object num){
		ByteBuffer bytes = null;
		byte sizeOfNum = getSizeOfNum(num);
		switch(num.getClass().getSimpleName()){
			case "Boolean":
				 bytes = ByteBuffer.allocate(sizeOfNum).put(((boolean) num) ? (byte) 1 : (byte) 0);
			case "Byte":
				bytes = ByteBuffer.allocate(sizeOfNum).put((byte) num);
			case "Short":
				bytes = ByteBuffer.allocate(sizeOfNum).putShort((short) num);
			case "Integer":
				bytes = ByteBuffer.allocate(sizeOfNum).putInt((int) num);
			case "Long":
				bytes = ByteBuffer.allocate(sizeOfNum).putLong((long) num);
			case "Float":
				bytes = ByteBuffer.allocate(sizeOfNum).putFloat((float) num);
			case "Double":
				bytes = ByteBuffer.allocate(sizeOfNum).putDouble((double) num);
			case "Char":
				bytes = ByteBuffer.allocate(sizeOfNum).putChar((char) num);
		}
			
		return bytes.array();		
	}
	
	public static byte getSizeOfNum(Object num){
		switch (num.getClass().getSimpleName()) {
			case "Boolean":
				 return SIZE_OF_BOOLEAN;
			case "Byte":
				return SIZE_OF_BYTE;
			case "Short":
				return SIZE_OF_SHORT;
			case "Integer":
				return SIZE_OF_INTEGER;
			case "Long":
				return SIZE_OF_LONG;
			case "Float":
				return SIZE_OF_FLOAT;
			case "Double":
				return SIZE_OF_DOUBLE;
			case "Char":
				return SIZE_OF_CHARACTER;
			default:
				assert false;
				return 8;
		}
	}
	
	public static int unsignByte(byte b){
		return (b & 0xFF);
	}
	
	public static int unsignShort(short s){
		return (s & 0xFFFF);
	}
	
	public static boolean checkByteWithAnd(byte[] bytes, int index, byte value){
		return (bytes[index] & value) == value;
	}
}
