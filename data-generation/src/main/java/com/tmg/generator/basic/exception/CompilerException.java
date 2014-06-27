package com.tmg.generator.basic.exception;

/**
 * This is the Custom Exception class. It will used to pass the Compiler
 * Exception to the next Layer.
 * 
 * @author Govind Gaikwad
 * 
 * @version 1.0 June 27, 2014.
 */
public class CompilerException extends Exception {

	private static final long serialVersionUID = 5640502737112432731L;

	public CompilerException() {
		super();
	}

	public CompilerException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CompilerException(String message, Throwable cause) {
		super(message, cause);
	}

	public CompilerException(String message) {
		super(message);
	}

	public CompilerException(Throwable cause) {
		super(cause);
	}

}
