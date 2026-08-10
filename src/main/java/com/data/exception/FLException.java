package com.data.exception;

public class FLException extends Exception {
	
	private static final long serialVersionUID = 5606932556357539286L;

	public FLException(String message) {
		super(message);
	}

	public FLException(Exception e) {		
		this(e.toString());
	}
}
