/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.feature.extension.apiregions.api.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/** 
 * A configurable entity has properties
 */
public abstract class ConfigurableEntity extends DescribableEntity {
	
	/** The properties */
    private final Map<String, Property> properties = new LinkedHashMap<>();

    /**
     * Clear the object and remove all metadata
     */
	public void clear() {
        super.clear();
		this.properties.clear();
    }

	/**
	 * Extract the metadata from the JSON object.
	 * This method first calls {@link #clear()}
	 * @param jsonObj The JSON Object
	 * @throws IOException If JSON parsing fails
	 */
	public void fromJSONObject(final JsonObject jsonObj) throws IOException {
        super.fromJSONObject(jsonObj);
        try {
            final JsonValue val = this.getAttributes().remove(InternalConstants.KEY_PROPERTIES);
            if ( val != null ) {
                for(final Map.Entry<String, JsonValue> innerEntry : val.asJsonObject().entrySet()) {
					final Property prop = new Property();
					prop.fromJSONObject(innerEntry.getValue().asJsonObject());
                    this.getProperties().put(innerEntry.getKey(), prop);
                }
            }            
        } catch (final JsonException | IllegalArgumentException e) {
            throw new IOException(e);
        }
	}

	/**
	 * Get the properties
	 * @return The properties
	 */
    public Map<String, Property> getProperties() {
        return this.properties;
    }

    /**
     * Convert this object into JSON
     *
     * @return The json object builder
     * @throws IOException If generating the JSON fails
     */
	JsonObjectBuilder createJson() throws IOException {
		final JsonObjectBuilder objBuilder = super.createJson();

		if ( !this.getProperties().isEmpty() ) {
			final JsonObjectBuilder propBuilder = Json.createObjectBuilder();
			for(final Map.Entry<String, Property> entry : this.getProperties().entrySet()) {
				propBuilder.add(entry.getKey(), entry.getValue().createJson());
			}
			objBuilder.add(InternalConstants.KEY_PROPERTIES, propBuilder);
		}

		return objBuilder;
   }
}
