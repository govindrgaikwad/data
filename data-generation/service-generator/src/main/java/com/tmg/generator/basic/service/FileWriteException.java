package com.tmg.generator.basic.service;

public class FileWriteException extends Exception {

	private static final long serialVersionUID = 5640502737112432731L;

	public FileWriteException() {
		super();
	}

	public FileWriteException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FileWriteException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileWriteException(String message) {
		super(message);
	}

	public FileWriteException(Throwable cause) {
		super(cause);
	}

}
