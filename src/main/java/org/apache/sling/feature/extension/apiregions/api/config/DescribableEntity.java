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

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Abstract class for all describable entities, having an optional title,
 * description and deprecation info.
 */
public abstract class DescribableEntity extends AttributeableEntity {
	
	/** The title */
    private String title;

	/** The description */
    private String description;

	/** The optional deprecation text */
	private String deprecated;

	/**
     * Clear the object and reset to defaults
     */
	public void clear() {
		super.clear();
		this.setTitle(null);
		this.setDescription(null);
		this.setDeprecated(null);
	}
	
	/**
	 * Extract the metadata from the JSON object.
	 * This method first calls {@link #clear()}
     * 
	 * @param jsonObj The JSON Object
	 * @throws IOException If JSON parsing fails
	 */
	public void fromJSONObject(final JsonObject jsonObj) throws IOException {
		super.fromJSONObject(jsonObj);
        try {
			this.setTitle(this.getString(InternalConstants.KEY_TITLE));
			this.setDescription(this.getString(InternalConstants.KEY_DESCRIPTION));
			this.setDeprecated(this.getString(InternalConstants.KEY_DEPRECATED));
        } catch (final JsonException | IllegalArgumentException e) {
            throw new IOException(e);
		}
	}

	/**
	 * Get the title
	 * @return The title or {@code null}
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title
	 * @param title the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Get the description
	 * @return the description or {@code null}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description
	 * @param description the description to set
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Get the deprecation text
	 * @return the deprecation text or {@code null}
	 */
	public String getDeprecated() {
		return deprecated;
	}

	/**
	 * Set the deprecation text
	 * @param deprecated the deprecation text to set
	 */
	public void setDeprecated(final String deprecated) {
		this.deprecated = deprecated;
	}

	/**
     * Convert this object into JSON
     *
     * @return The json object builder
     * @throws IOException If generating the JSON fails
     */
    JsonObjectBuilder createJson() throws IOException {
		final JsonObjectBuilder objectBuilder = super.createJson();

		this.setString(objectBuilder, InternalConstants.KEY_TITLE, this.getTitle());
		this.setString(objectBuilder, InternalConstants.KEY_DESCRIPTION, this.getDescription());
		this.setString(objectBuilder, InternalConstants.KEY_DEPRECATED, this.getDeprecated());

		return objectBuilder;
	}
}
