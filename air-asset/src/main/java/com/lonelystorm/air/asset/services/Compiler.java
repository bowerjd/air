package com.lonelystorm.air.asset.services;

import com.lonelystorm.air.asset.exceptions.CompilerException;
import com.lonelystorm.air.asset.models.Asset;

public interface Compiler {

    String compile(Asset library, String file, String source) throws CompilerException;

    boolean supports(Asset library, String file);

}
