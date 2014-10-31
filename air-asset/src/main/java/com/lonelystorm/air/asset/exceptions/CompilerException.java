package com.lonelystorm.air.asset.exceptions;


public class CompilerException extends Exception {

    /**
     * Default Serial Version
     */
    private static final long serialVersionUID = 1L;

    public CompilerException(String message) {
        super(message);
    }

    public CompilerException(String message, Throwable cause) {
        super(message, cause);
    }

}
