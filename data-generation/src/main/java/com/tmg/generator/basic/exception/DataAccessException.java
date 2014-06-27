package com.tmg.generator.basic.exception;

/**
 * This is the Custom Exception class. It will used to pass the
 * SQLException/PersistenceException to the next Layer.
 * 
 * @author Govind Gaikwad
 * 
 * @version 1.0 June 27, 2014.
 */
public class DataAccessException extends Exception {

	private static final long serialVersionUID = 5640502737112432731L;

	public DataAccessException() {
		super();
	}

	public DataAccessException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataAccessException(String message) {
		super(message);
	}

	public DataAccessException(Throwable cause) {
		super(cause);
	}

}
