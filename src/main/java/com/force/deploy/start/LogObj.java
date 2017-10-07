package com.force.deploy.start;

public class LogObj {

	private String message;
	private Boolean isLogged;
	// Default constructor set default flag false
	public LogObj(){
		this.isLogged = false;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Boolean getIsLogged() {
		return isLogged;
	}
	public void setIsLogged(Boolean isLogged) {
		this.isLogged = isLogged;
	}
	
	
}
