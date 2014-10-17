package com.lonelystorm.air.component.model;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BsonIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bsonId;

    private String field;

    public String getBsonId() {
        return bsonId;
    }

    public String getField() {
        return field;
    }

    public static BsonIdentifier fromProperty(String property) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(property, BsonIdentifier.class);
    }

}
