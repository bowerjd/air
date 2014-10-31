package com.lonelystorm.air.asset.services;

import com.lonelystorm.air.asset.exceptions.CompilerException;
import com.lonelystorm.air.asset.models.Asset;

public interface CompilerManager {

    String compile(Asset asset) throws CompilerException;

}
