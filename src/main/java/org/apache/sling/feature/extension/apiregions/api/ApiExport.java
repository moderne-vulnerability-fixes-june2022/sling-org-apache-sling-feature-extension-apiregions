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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.sling.feature.ArtifactId;

/**
 * Describes an exported package.
 */
public class ApiExport implements Comparable<ApiExport> {

    private static final String DEPRECATED_KEY = "deprecated";

    private static final String MSG_KEY = "msg";

    private static final String SINCE_KEY = "since";

    private static final String MEMBERS_KEY = "members";

    private final String name;

    private volatile String toggle;

    private volatile ArtifactId previous;

    private final Map<String, String> properties = new HashMap<>();

    private final Deprecation deprecation = new Deprecation();

    /**
     * Create a new export
     *
     * @param name Package name for the export
     */
    public ApiExport(final String name) {
        if ( name == null ) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    /**
     * Get the package name
     *
     * @return The package name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the optional toggle information
     *
     * @return The toggle info or {@code null}
     */
    public String getToggle() {
        return toggle;
    }

    /**
     * Set the toggle info.
     *
     * @param toggle The toggle info
     */
    public void setToggle(String toggle) {
        this.toggle = toggle;
    }

    /**
     * Get the previous version of this api
     *
     * @return The previous version or {@code null}
     */
    public ArtifactId getPrevious() {
        return previous;
    }

    /**
     * Set the previous version
     *
     * @param previous Previus version
     */
    public void setPrevious(ArtifactId previous) {
        this.previous = previous;
    }

    /**
     * Get additional properties
     *
     * @return Modifiable map of properties
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * Get the deprecation info
     *
     * @return The info
     */
    public Deprecation getDeprecation() {
        return this.deprecation;
    }

    /**
     * Internal method to parse the extension JSON
     * @param dValue The JSON value
     * @throws IOException If the format is not correct
     */
    void parseDeprecation(final JsonValue dValue) throws IOException {
        if ( dValue.getValueType() == ValueType.STRING ) {

            // value is deprecation message for the whole package
            final DeprecationInfo info = new DeprecationInfo(((JsonString)dValue).getString());
            this.getDeprecation().setPackageInfo(info);

        } else if ( dValue.getValueType() == ValueType.OBJECT ) {

            // value is an object with properties
            final JsonObject depObj = dValue.asJsonObject();
            if ( depObj.containsKey(MSG_KEY) && depObj.containsKey(MEMBERS_KEY) ) {
                throw new IOException("Export " + this.getName() + " has wrong info in " + DEPRECATED_KEY);
            }
            if ( !depObj.containsKey(MSG_KEY) && !depObj.containsKey(MEMBERS_KEY)) {
                throw new IOException("Export " + this.getName() + " has missing info in " + DEPRECATED_KEY);
            }
            if ( depObj.containsKey(MSG_KEY) ) {
                // whole package
                final DeprecationInfo info = new DeprecationInfo(depObj.getString(MSG_KEY));
                info.setSince(depObj.getString(SINCE_KEY, null));
                this.getDeprecation().setPackageInfo(info);
            } else {
                if ( depObj.containsKey(SINCE_KEY) ) {
                    throw new IOException("Export " + this.getName() + " has wrong since in " + DEPRECATED_KEY);
                }
                final JsonValue val = depObj.get(MEMBERS_KEY);
                if ( val.getValueType() != ValueType.OBJECT) {
                    throw new IOException("Export " + this.getName() + " has wrong type for " + MEMBERS_KEY + " : " + val.getValueType().name());
                }
                for (final Map.Entry<String, JsonValue> memberProp : val.asJsonObject().entrySet()) {
                    if ( memberProp.getValue().getValueType() == ValueType.STRING ) {
                        final DeprecationInfo info = new DeprecationInfo(((JsonString)memberProp.getValue()).getString());
                        this.getDeprecation().addMemberInfo(memberProp.getKey(), info);
                    } else if ( memberProp.getValue().getValueType() == ValueType.OBJECT ) {
                        final JsonObject memberObj = memberProp.getValue().asJsonObject();
                        if ( !memberObj.containsKey(MSG_KEY) ) {
                            throw new IOException("Export " + this.getName() + " has wrong type for member in " + MEMBERS_KEY + " : " + memberProp.getValue().getValueType().name());
                        }
                        final DeprecationInfo info = new DeprecationInfo(memberObj.getString(MSG_KEY));
                        info.setSince(memberObj.getString(SINCE_KEY, null));
                        this.getDeprecation().addMemberInfo(memberProp.getKey(), info);
                    } else {
                        throw new IOException("Export " + this.getName() + " has wrong type for member in " + MEMBERS_KEY + " : " + memberProp.getValue().getValueType().name());
                    }
                }
            }
        } else {
            throw new IOException("Export " + this.getName() + " has wrong type for " + DEPRECATED_KEY + " : " + dValue.getValueType().name());
        }
    }

    /**
     * Internal method to create the JSON if deprecation is set
     * @return The JSON value or {@code null}
     */
    JsonValue deprecationToJSON() {
        final Deprecation dep = this.getDeprecation();
        if ( dep.getPackageInfo() != null ) {
            if ( dep.getPackageInfo().getSince() == null ) {
                return Json.createValue(dep.getPackageInfo().getMessage());
            } else {
                final JsonObjectBuilder depBuilder = Json.createObjectBuilder();
                depBuilder.add(MSG_KEY, dep.getPackageInfo().getMessage());
                depBuilder.add(SINCE_KEY, dep.getPackageInfo().getSince());

                return depBuilder.build();
            }
        } else if ( !dep.getMemberInfos().isEmpty() ) {
            final JsonObjectBuilder depBuilder = Json.createObjectBuilder();
            final JsonObjectBuilder membersBuilder = Json.createObjectBuilder();
            for(final Map.Entry<String, DeprecationInfo> memberEntry : dep.getMemberInfos().entrySet()) {
                if ( memberEntry.getValue().getSince() == null ) {
                    membersBuilder.add(memberEntry.getKey(), memberEntry.getValue().getMessage());
                } else {
                    final JsonObjectBuilder mBuilder = Json.createObjectBuilder();
                    mBuilder.add(MSG_KEY, memberEntry.getValue().getMessage());
                    mBuilder.add(SINCE_KEY, memberEntry.getValue().getSince());

                    membersBuilder.add(memberEntry.getKey(), mBuilder);
                }
            }

            depBuilder.add(MEMBERS_KEY, membersBuilder);
            return depBuilder.build();
        }
        return null;
    }

    @Override
    public int compareTo(final ApiExport o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "ApiExport [name=" + name + ", toggle=" + toggle + ", previous=" + previous + ", properties="
                + properties + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(deprecation, name, previous, properties, toggle);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ApiExport other = (ApiExport) obj;
        return Objects.equals(deprecation, other.deprecation) && Objects.equals(name, other.name)
                && Objects.equals(previous, other.previous) && Objects.equals(properties, other.properties)
                && Objects.equals(toggle, other.toggle);
    }
}
