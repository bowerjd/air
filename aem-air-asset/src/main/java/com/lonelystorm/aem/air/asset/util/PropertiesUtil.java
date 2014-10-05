package com.lonelystorm.aem.air.asset.util;

import org.apache.sling.api.resource.ValueMap;

public class PropertiesUtil {

    private PropertiesUtil() {
    };

    public static boolean containsProperty(ValueMap properties, String key) {
        return properties.containsKey(key);
    }

    public static <T extends Object> boolean comparePropertyValue(ValueMap properties, String key, T expected) {
        if (containsProperty(properties, key)) {
            Object value = properties.get(key, expected.getClass());
            return expected.equals(value);
        }

        return false;
    }

}
