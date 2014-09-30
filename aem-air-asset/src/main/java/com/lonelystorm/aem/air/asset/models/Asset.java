package com.lonelystorm.aem.air.asset.models;

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

    private String path;

    private Set<String> sources;

    public String[] getLoadPaths() {
        return loadPaths;
    }

    public String getPath() {
        return path;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    public Set<String> getSources() {
        return sources;
    }

    protected void setSources(Resource resource) {
        Iterable<Resource> children = resource.getChildren();

        sources = new HashSet<>();
        for (Resource child : children) {
            if (child.isResourceType(JcrConstants.NT_FILE)) {
                sources.add(child.getPath());
            }
        }
    }

}
