package com.mrsmyx.utils;

import com.mrsmyx.JMAPI;

public class Response {
	private boolean success;
	private String response;
	private JMAPI.PS3MAPI_RESPONSECODE responseCode;
	
	public static Response Build(boolean success, String response){
		return new Response().setResponse(response).setSuccess(success);
	}
	
	public static Response Build(boolean success, String response, JMAPI.PS3MAPI_RESPONSECODE responseCode){
		return new Response().setResponse(response).setSuccess(success).setResponseCode(responseCode);
	}
	
	public boolean getSuccess(){
		return success;
	}
	
	public String getResponse(){
		return response;
	}
	
	public JMAPI.PS3MAPI_RESPONSECODE getResponseCode(){
		return responseCode;
	}
	
	public Response setResponseCode(JMAPI.PS3MAPI_RESPONSECODE responseCode){
		this.responseCode = responseCode;
		return this;
	}
	
	public Response setSuccess(boolean success){
		this.success = success;
		return this;
	}
	
	public Response setResponse(String response){
		this.response = response;
		return this;
	}
}
