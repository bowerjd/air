package com.lonelystorm.air.asset.services;

public interface FileResolver {

    /**
     * Determines if a file exists in the repository.
     *
     * @param location
     * @return
     */
    boolean exists(String location);

    /**
     * Loads the content of a file from the repository.
     *
     * @param location
     * @return
     */
    String load(String location);

}
