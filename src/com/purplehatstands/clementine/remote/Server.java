package com.purplehatstands.clementine.remote;

import java.io.Serializable;

public class Server implements Serializable {
	private static final long serialVersionUID = 941813654087183935L;
	private String name_;
	private String address_;
	private int port_;
	
	public Server(String name, String address, int port) {
		name_ = name;
		address_ = address;
		port_ = port;
	}
	
	public String getName() {
		return name_;
	}
	
	public String getAddress() {
		return address_;
	}
	
	public int getPort() {
		return port_;
	}
}
