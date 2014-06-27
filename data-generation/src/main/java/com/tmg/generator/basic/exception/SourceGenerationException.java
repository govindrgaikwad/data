package com.tmg.generator.basic.exception;

/**
 * This is the Custom Exception class. It will used to pass the Apache Velocity
 * Related Exception to the next Layer.
 * 
 * @author Govind Gaikwad
 * 
 * @version 1.0 June 27, 2014.
 */
public class SourceGenerationException extends Exception {

	private static final long serialVersionUID = 5640502737112432731L;

	public SourceGenerationException() {
		super();
	}

	public SourceGenerationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SourceGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SourceGenerationException(String message) {
		super(message);
	}

	public SourceGenerationException(Throwable cause) {
		super(cause);
	}

}
