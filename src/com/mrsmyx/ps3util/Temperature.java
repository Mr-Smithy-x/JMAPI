package com.mrsmyx.ps3util;

import com.mrsmyx.utils.KeyValuePair;

public final class Temperature extends KeyValuePair<String, String> {

	protected Temperature(String key, String value){
		super(key,value);
	}

	public String getCPUF(){
		return String.valueOf(Integer.valueOf(super.getKey()) * 9/5 + 32);
	}
	
	public String getRSXF(){
		return String.valueOf(Integer.valueOf(super.getValue()) * 9/5 + 32);
	}
	
	public String getCPU(){
		return super.getKey();
	}
	
	public String getRSX(){
		return super.getValue();
	}
	
	
	
	public static Temperature instantiate(String cpu, String rsx){
		return new Temperature(cpu, rsx);
	}
	
}
