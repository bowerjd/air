package com.lonelystorm.aem.air.asset.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Optional;

public abstract class Asset {

    @Inject
    @Optional
    @Default(values = {})
    private String[] loadPaths;

    @Inject
    @Optional
    @Default(values = {})
    private String[] embed;

    private String path;

    private Set<String> sources;

    /**
     * Returns an array containing the load paths for the compiler.
     *
     * @return The load paths
     */
    public String[] getLoadPaths() {
        return loadPaths.clone();
    }

    /**
     * Returns an array containing the categories of assets to embed during compilation.
     *
     * @return The embed dependencies
     */
    public String[] getEmbed() {
        return embed.clone();
    }

    /**
     * Returns the path of the asset.
     *
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path of the asset.
     *
     * @param path The path
     */
    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the sources of the asset.
     *
     * @return The sources
     */
    public Set<String> getSources() {
        return Collections.unmodifiableSet(sources);
    }

    /**
     * Sets the sources of the asset.
     *
     * @param resource The resource to parse
     */
    protected void setSources(Resource resource) {
        Iterable<Resource> children = resource.getChildren();

        sources = new HashSet<>();
        for (Resource child : children) {
            if (child.isResourceType(JcrConstants.NT_FILE)) {
                sources.add(child.getPath());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode() + ((path == null) ? 0 : path.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Asset) {
            return path.equals(((Asset) obj).path);
        }

        return false;
    }

}
