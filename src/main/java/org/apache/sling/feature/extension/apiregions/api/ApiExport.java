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
 * 
 * This class is not thread safe.
 */
public class ApiExport implements Comparable<ApiExport> {

    private static final String DEPRECATED_KEY = "deprecated";

    private static final String MSG_KEY = "msg";

    private static final String SINCE_KEY = "since";

    private static final String MEMBERS_KEY = "members";

    private static final String NAME_KEY = "name";

    private static final String TOGGLE_KEY = "toggle";

    private static final String PREVIOUS_KEY = "previous";

    private static final String PREVIOUS_ARTIFACT_ID_KEY = "previous-artifact-id";

    private static final String PREVIOUS_PACKAGE_VERSION_KEY = "previous-package-version";

    private final String name;

    private String toggle;

    /** If the package is behind a toggle, this is the previous artifact containing the package not behind a toggle */
    private ArtifactId previousArtifactId;

    /** If the package is behind a toggle, this is the previous version of the package not behind a toggle */
    private String previousPackageVersion;

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
     * Get the previous version of this package
     * @return The previous version of this package or {@code null}
     * @since 1.2.0
     */
    public String getPreviousPackageVersion() {
        return this.previousPackageVersion;
    }

    /**
     * Set the previous version of this package
     * @param version The previous version of this package 
     * @since 1.2.0
     */
    public void setPreviousPackageVersion(final String version) {
        this.previousPackageVersion = version;
    }

    /**
     * Get the previous artifact id containing the previous version
     *
     * @return The previous artifact id or {@code null}
     * @since 1.2.0
     */
    public ArtifactId getPreviousArtifactId() {
        return previousArtifactId;
    }

    /**
     * Set the previous artifact id
     *
     * @param previous Previus artifact id
     * @since 1.2.0
     */
    public void setPreviousArtifactId(final ArtifactId previous) {
        this.previousArtifactId = previous;
    }

    /**
     * Get the previous version of this api
     *
     * @return The previous version or {@code null}
     * @deprecated Use {@link #getPreviousArtifactId()}
     */
    public ArtifactId getPrevious() {
        return this.getPreviousArtifactId();
    }

    /**
     * Set the previous version
     *
     * @param previous Previus version
     * @deprecated Use {@link #setPreviousArtifactId(ArtifactId)}
     */
    public void setPrevious(final ArtifactId previous) {
        this.setPreviousArtifactId(previous);
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

    JsonValue toJSONValue() {
        final JsonValue depValue = this.deprecationToJSON();
        if (this.getToggle() == null 
            && this.getPreviousPackageVersion() == null 
            && this.getPreviousArtifactId() == null
            && this.getProperties().isEmpty() 
            && depValue == null ) {
           return Json.createValue(this.getName());
        }
        final JsonObjectBuilder expBuilder = Json.createObjectBuilder();
        expBuilder.add(NAME_KEY, this.getName());
        if (this.getToggle() != null) {
            expBuilder.add(TOGGLE_KEY, this.getToggle());
        }
        if (this.getPreviousPackageVersion() != null) {
            expBuilder.add(PREVIOUS_PACKAGE_VERSION_KEY, this.getPreviousPackageVersion());
        }
        if (this.getPreviousArtifactId() != null) {
            expBuilder.add(PREVIOUS_ARTIFACT_ID_KEY, this.getPreviousArtifactId().toMvnId());
        }

        if ( depValue != null ) {
            expBuilder.add(DEPRECATED_KEY, depValue);
        }

        for (final Map.Entry<String, String> entry : this.getProperties().entrySet()) {
            expBuilder.add(entry.getKey(), entry.getValue());
        }
        return expBuilder.build();
    }

    static ApiExport fromJson(final ApiRegion region, final JsonValue val) throws IOException {
        if (val.getValueType() == ValueType.STRING) {
            final String name = ((JsonString) val).getString();
            if (!name.startsWith("#")) {
                final ApiExport export = new ApiExport(name);
                if (!region.add(export)) {
                    throw new IOException("Export " + export.getName()
                            + " is defined twice in region " + region.getName());
                }
                return export;
            }
            return null;
        } else if (val.getValueType() == ValueType.OBJECT) {
            final JsonObject expObj = (JsonObject) val;
            final ApiExport export = new ApiExport(expObj.getString(NAME_KEY));
            if (!region.add(export)) {
                throw new IOException("Export " + export.getName() + " is defined twice in region "
                        + region.getName());
            }

            boolean setPreviousArtifact = false;
            for (final String key : expObj.keySet()) {
                if (NAME_KEY.equals(key)) {
                    continue; // already set

                } else if (TOGGLE_KEY.equals(key)) {
                    export.setToggle(expObj.getString(key));

                } else if (PREVIOUS_PACKAGE_VERSION_KEY.equals(key)) {
                    export.setPreviousPackageVersion(expObj.getString(key));
                } else if (PREVIOUS_KEY.equals(key)) {
                    if ( setPreviousArtifact ) {
                        throw new IOException("Export " + export.getName() + " is defining previous artifact id twice in region "
                                + region.getName());
                    }
                    export.setPreviousArtifactId(ArtifactId.parse(expObj.getString(key)));
                    setPreviousArtifact = true;
                } else if (PREVIOUS_ARTIFACT_ID_KEY.equals(key)) {
                    if ( setPreviousArtifact ) {
                        throw new IOException("Export " + export.getName() + " is defining previous artifact id twice in region "
                                + region.getName());
                    }
                    export.setPreviousArtifactId(ArtifactId.parse(expObj.getString(key)));
                    setPreviousArtifact = true;

                } else if ( DEPRECATED_KEY.equals(key)) {
                    final JsonValue dValue = expObj.get(DEPRECATED_KEY);
                    export.parseDeprecation(dValue);

                    // everything else is stored as a string property
                } else {
                    export.getProperties().put(key, expObj.getString(key));
                }
            }
            return export;
        } else {
            throw new IOException("Region " + region.getName() + " has wrong type for package export : " + val.getValueType().name());
        }
    }

    @Override
    public int compareTo(final ApiExport o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "ApiExport [name=" + name + ", toggle=" + toggle + ", previousPackageVersion=" + previousPackageVersion
                + ", previousArtifactId=" + previousArtifactId + ", properties=" + properties + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(deprecation, name, previousPackageVersion, previousArtifactId, properties, toggle);
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
                && Objects.equals(previousArtifactId, other.previousArtifactId)
                && Objects.equals(previousPackageVersion, other.previousPackageVersion) && Objects.equals(properties, other.properties)
                && Objects.equals(toggle, other.toggle);
    }
}
