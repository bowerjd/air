package org.apache.sling.models.impl;

import java.util.HashMap;

import org.osgi.framework.Constants;

@SuppressWarnings("serial")
public class ServicePropertiesMap extends HashMap<String, Object> {

    public ServicePropertiesMap(long serviceId, int serviceRanking) {
        super();
        put(Constants.SERVICE_ID, serviceId);
        put(Constants.SERVICE_RANKING, serviceRanking);
    }

}
