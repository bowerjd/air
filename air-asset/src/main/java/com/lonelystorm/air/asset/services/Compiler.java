package com.lonelystorm.air.asset.services;

import com.lonelystorm.air.asset.exceptions.CompilerException;
import com.lonelystorm.air.asset.models.Asset;

public interface Compiler {

    /**
     * @param library The library that is being compiled
     * @param file This is used solely for reporting errors
     * @param source The source to compile
     * @return
     * @throws CompilerException
     */
    String compile(Asset library, String file, String source) throws CompilerException;

    /**
     * Does the compiler support compiling the given file.
     * 
     * @param library the library in question (not used at the moment)
     * @param file a file in the library to compile
     * @return true if <code>file</code> does not start with an underscore "_"
     *         and has an extension of "scss" or "sass".
     */
    boolean supports(Asset library, String file);

}
