package com.lonelystorm.aem.air.asset.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.adobe.cq.commerce.common.ValueMapDecorator;

@RunWith(MockitoJUnitRunner.class)
public class PropertiesUtilTest {

    private ValueMap properties;

    @Before
    public void setUp() {
        Map<String, Object> values = new TreeMap<>();
        values.put("existantValue", true);

        values.put("stringValue", "String");
        values.put("booleanValue", true);
        values.put("intValue", 30);

        properties = new ValueMapDecorator(values);
    }

    @Test
    public void containsProperty() {
        assertTrue(PropertiesUtil.containsProperty(properties, "existantValue"));

        assertFalse(PropertiesUtil.containsProperty(properties, "nonExistantValue"));
    }

    @Test
    public void comparePropertyValue() {
        assertTrue(PropertiesUtil.comparePropertyValue(properties, "stringValue", "String"));
        assertTrue(PropertiesUtil.comparePropertyValue(properties, "booleanValue", true));
        assertTrue(PropertiesUtil.comparePropertyValue(properties, "intValue", 30));

        assertFalse(PropertiesUtil.comparePropertyValue(properties, "nonExistantStringValue", "String"));
        assertFalse(PropertiesUtil.comparePropertyValue(properties, "nonExistantBooleanValue", true));
        assertFalse(PropertiesUtil.comparePropertyValue(properties, "nonExistantIntValue", 30));
    }

}
