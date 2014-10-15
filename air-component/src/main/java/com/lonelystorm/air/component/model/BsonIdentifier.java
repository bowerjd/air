package com.lonelystorm.air.component.model;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

public class BsonIdentifier {

    private String id;

    public String getId() {
        return id;
    }

    public static BsonIdentifier fromProperty(String property) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(property, BsonIdentifier.class);
    }

}
