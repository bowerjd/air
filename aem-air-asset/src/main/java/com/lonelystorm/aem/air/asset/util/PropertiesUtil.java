package com.lonelystorm.aem.air.asset.util;

import org.apache.sling.api.resource.ValueMap;

public class PropertiesUtil {

    private PropertiesUtil() {
    };

    public static boolean containsProperty(ValueMap properties, String key) {
        return properties.containsKey(key);
    }

    public static <T extends Object> boolean comparePropertyValue(ValueMap properties, String key, T value) {
        if (properties.containsKey(key) && properties.get(key, value.getClass()).equals(value)) {
            return true;
        } else {
            return false;
        }
    }

}
