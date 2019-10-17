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
package org.apache.sling.feature.extension.apiregions.api;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;

import org.apache.sling.feature.ArtifactId;

/**
 * An api regions configuration
 */
public class ApiRegions {

    /** The name of the api regions extension. */
    public static final String EXTENSION_NAME = "api-regions";

    private static final String NAME_KEY = "name";

    private static final String EXPORTS_KEY = "exports";

    private static final String TOGGLE_KEY = "toggle";

    private static final String PREVIOUS_KEY = "previous";

    private final List<ApiRegion> regions = new ArrayList<>();

    public List<ApiRegion> getRegions() {
        return this.regions;
    }

    /**
     * Add the region. The region is only added if there isn't already a region with
     * the same name
     *
     * @param region The region to add
     * @return {@code true} if the region could be added, {@code false} otherwise
     */
    public boolean addUniqueRegion(final ApiRegion region) {
        boolean found = false;
        for (final ApiRegion c : this.regions) {
            if (c.getName().equals(region.getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.regions.add(region);
        }
        return !found;
    }

    /**
     * Get a named region
     *
     * @param name The name
     * @return The region or {@code null}
     */
    public ApiRegion getRegionByName(final String name) {
        ApiRegion found = null;

        for (final ApiRegion c : this.regions) {
            if (c.getName().equals(name)) {
                found = c;
                break;
            }
        }

        return found;
    }

    /**
     * Get the names of the regions
     *
     * @return The list of regions, might be empty
     */
    public List<String> getRegionNames() {
        final List<String> names = new ArrayList<>();
        for (final ApiRegion c : this.regions) {
            names.add(c.getName());
        }
        return Collections.unmodifiableList(names);
    }

    /**
     * Convert regions into json
     *
     * @return The json array
     */
    public JsonArray toJSONArray() {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (final ApiRegion region : this.getRegions()) {
            final JsonObjectBuilder regionBuilder = Json.createObjectBuilder();
            regionBuilder.add(NAME_KEY, region.getName());

            if (!region.getExports().isEmpty()) {
                final JsonArrayBuilder expArrayBuilder = Json.createArrayBuilder();
                for (final ApiExport exp : region.getExports()) {
                    if (exp.getToggle() == null) {
                        expArrayBuilder.add(exp.getName());
                    } else {
                        final JsonObjectBuilder expBuilder = Json.createObjectBuilder();
                        expBuilder.add(NAME_KEY, exp.getName());
                        expBuilder.add(TOGGLE_KEY, exp.getToggle());
                        if (exp.getPrevious() != null) {
                            expBuilder.add(PREVIOUS_KEY, exp.getPrevious().toMvnId());
                        }
                        expArrayBuilder.add(expBuilder);
                    }
                }

                regionBuilder.add(EXPORTS_KEY, expArrayBuilder);
            }
            for (final Map.Entry<String, String> entry : region.getProperties().entrySet()) {
                regionBuilder.add(entry.getKey(), entry.getValue());
            }

            arrayBuilder.add(regionBuilder);
        }

        return arrayBuilder.build();
    }

    /**
     * Convert regions into json
     *
     * @return The json array as a string
     */
    public String toJSON() {
        final JsonArray array = this.toJSONArray();
        try (final StringWriter stringWriter = new StringWriter();
                final JsonWriter writer = Json.createWriter(stringWriter)) {
            writer.writeArray(array);
            return stringWriter.toString();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Parse a JSON array into an api regions object
     *
     * @param json The json as a string
     * @return The api regions
     */
    public static ApiRegions parse(final String json) {
        try (final JsonReader reader = Json.createReader(new StringReader(json))) {
            return parse(reader.readArray());
        }
    }

    /**
     * Parse a JSON array into an api regions object
     *
     * @param json The json
     * @return The api regions
     */
    public static ApiRegions parse(final JsonArray json) {
        final ApiRegions regions = new ApiRegions();

        for (final JsonValue value : json) {
            if (value.getValueType() != ValueType.OBJECT) {
                throw new IllegalArgumentException("Illegal api regions json " + json);
            }
            final ApiRegion region = new ApiRegion();

            final JsonObject obj = (JsonObject) value;
            region.setName(obj.getString(NAME_KEY));

            for(final Map.Entry<String, JsonValue> entry : obj.entrySet()) {
                if ( NAME_KEY.equals(entry.getKey()) ) {
                    region.setName(obj.getString(NAME_KEY));
                } else if (entry.getKey().equals(EXPORTS_KEY)) {
                    for (final JsonValue e : (JsonArray)entry.getValue()) {
                        if (e.getValueType() == ValueType.STRING) {
                            final String name = ((JsonString) e).getString();
                            if (!name.startsWith("#")) {
                                final ApiExport export = new ApiExport();
                                region.getExports().add(export);

                                export.setName(name);
                            }
                        } else if (e.getValueType() == ValueType.OBJECT) {
                            final JsonObject expObj = (JsonObject) e;
                            final ApiExport export = new ApiExport();
                            region.getExports().add(export);

                            export.setName(expObj.getString(NAME_KEY));
                            export.setToggle(expObj.getString(TOGGLE_KEY, null));
                            if (expObj.containsKey(PREVIOUS_KEY)) {
                                export.setPrevious(ArtifactId.parse(expObj.getString(PREVIOUS_KEY)));
                            }
                        }
                    }
                } else {
                    region.getProperties().put(entry.getKey(), ((JsonString) entry.getValue()).getString());
                }
            }
            if (!regions.addUniqueRegion(region)) {
                throw new IllegalArgumentException("Region " + region.getName() + " is defined twice");
            }

        }
        return regions;
    }

    @Override
    public String toString() {
        return "ApiRegions [regions=" + regions + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((regions == null) ? 0 : regions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApiRegions other = (ApiRegions) obj;
        if (regions == null) {
            if (other.regions != null)
                return false;
        } else if (!regions.equals(other.regions))
            return false;
        return true;
    }
}
