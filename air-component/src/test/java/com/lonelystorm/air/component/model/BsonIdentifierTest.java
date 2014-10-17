package com.lonelystorm.air.component.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BsonIdentifierTest {

    @Test
    public void fromProperty() throws Exception {
        BsonIdentifier bsonIdentifier = BsonIdentifier.fromProperty("{ \"bsonId\": \"a\", \"field\": \"Title\" }");

        assertNotNull(bsonIdentifier);
        assertEquals("a", bsonIdentifier.getBsonId());
        assertEquals("Title", bsonIdentifier.getField());
    }

    @Test
    public void fromPropertyEmpty() throws Exception {
        BsonIdentifier bsonIdentifier = BsonIdentifier.fromProperty("{}");

        assertNotNull(bsonIdentifier);
        assertNull(bsonIdentifier.getBsonId());
    }

}
