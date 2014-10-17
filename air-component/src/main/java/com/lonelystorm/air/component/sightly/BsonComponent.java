package com.lonelystorm.air.component.sightly;

import static org.apache.commons.lang.ArrayUtils.isNotEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUse;
import com.lonelystorm.air.component.model.BsonIdentifier;

public class BsonComponent extends WCMUse {

    private final static Logger LOGGER = LoggerFactory.getLogger(BsonComponent.class);

    private String property;

    @Override
    public void activate() throws Exception {
        property = get("property", String.class);
    }

    public String getName() {
        return property;
    }

    public List<BsonIdentifier> getRows() {
        List<BsonIdentifier> rows = new ArrayList<>();

        String[] elements = getProperties().get(property, String[].class);
        if (isNotEmpty(elements)) {
            for (String element : elements) {
                try {
                    BsonIdentifier bsonIdentifier = BsonIdentifier.fromProperty(element);
                    if (bsonIdentifier != null) {
                        rows.add(bsonIdentifier);
                    }
                } catch (IOException e) {
                    LOGGER.error("Error converting property to bson identifier", e);
                }
            }
        }

        return rows;
    }

}
