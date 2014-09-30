package com.lonelystorm.aem.air.asset.models;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

@Model(adaptables = Resource.class)
public class AssetTheme extends Asset {

    @Self
    private Resource resource;

    @Inject
    private String[] themes;

    public String[] getThemes() {
        return themes;
    }

    @PostConstruct
    protected void construct() {
        setPath(resource.getPath());
        setSources(resource);
    }

}
