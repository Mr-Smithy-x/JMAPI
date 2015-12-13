package com.mrsmyx.utils;

public class KeyValuePair<K,V>{
	private K key;
	private V value;
	
	public KeyValuePair (K key, V value){
		this.key = key;
		this.value = value;
	}
	
	public KeyValuePair(){}
	
	protected K getKey(){
		return key;
	}
	
	
	protected V getValue(){
		return value;
	}
	
	protected void setKey(K key){
		this.key = key;
	}
	
	protected void setValue(V value){
		this.value = value;
	}
}
