package com.lonelystorm.air.asset.exceptions;

/**
 * Exception thrown by Compilers if an error occurs whilst attempting
 * to compile the source asset.
 */
public class CompilerException extends Exception {

    /**
     * Serial Version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public CompilerException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public CompilerException(String message, Throwable cause) {
        super(message, cause);
    }

}
