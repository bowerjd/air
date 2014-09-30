package com.lonelystorm.aem.air.asset.util;

import org.apache.sling.api.resource.ValueMap;

public class PropertiesUtil {

    public static boolean containsProperty(ValueMap properties, String key) {
        return properties.containsKey(key);
    }

    public static boolean comparePropertyValue(ValueMap properties, String key, String value) {
        if (properties.containsKey(key) && properties.get(key, String.class).equals(value)) {
            return true;
        } else {
            return false;
        }
    }

}
