package com.mrsmyx.ps3util;

public class PS3Process {
	public String title;
	public String process;
	
	public static PS3Process Build(String title, String process){
		return new PS3Process().setTitle(title).setProcess(process);
	}
	
	protected PS3Process setTitle(String title){
		this.title = title;
		return this;
	}
	
	protected PS3Process setProcess(String process){
		this.process = process;
		return this;
	}
	
	public String getTitle() {
		return title;
	}
	public String getProcess() {
		return process;
	}
}
