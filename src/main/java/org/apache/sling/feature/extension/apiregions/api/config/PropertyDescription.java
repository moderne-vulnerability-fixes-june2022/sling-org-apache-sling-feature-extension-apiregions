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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Instances of this class represent a single configuration property
 */
public class PropertyDescription extends DescribableEntity {
	
	/** The property type */
	private PropertyType type;

	/** The property cardinality */
    private int cardinality;

	/** The optional variable */
    private String variable;

	/** The optional range */
	private Range range;

	/** The required includes for an array/collection (optional) */
	private String[] includes;

	/** The required excludes for an array/collection (optional) */
	private String[] excludes;

	/** The optional list of options for the value */
	private List<Option> options;
	
	/** The optional regex */
	private Pattern pattern;

	/** Required? */
	private boolean required;
    
    /**
     * Create a new description
     */
    public PropertyDescription() {
        this.setDefaults();
    }

    void setDefaults() {
		this.setType(PropertyType.STRING);
        this.setCardinality(1);
        this.setRequired(false);
    }

    /**
     * Clear the object and reset to defaults
     */
    @Override
	public void clear() {
        super.clear();
        this.setDefaults();
		this.setVariable(null);
		this.setRange(null);
		this.setIncludes(null);
		this.setExcludes(null);
		this.setOptions(null);
		this.setRegex(null);
    }

	/**
	 * Extract the metadata from the JSON object.
	 * This method first calls {@link #clear()}
	 * @param jsonObj The JSON Object
	 * @throws IOException If JSON parsing fails
	 */
    @Override
	public void fromJSONObject(final JsonObject jsonObj) throws IOException {
        super.fromJSONObject(jsonObj);
        try {
			this.setVariable(this.getString(InternalConstants.KEY_VARIABLE));
			this.setCardinality(this.getInteger(InternalConstants.KEY_CARDINALITY, this.getCardinality()));
			this.setRequired(this.getBoolean(InternalConstants.KEY_REQUIRED, this.isRequired()));
			
			final String typeVal = this.getString(InternalConstants.KEY_TYPE);
			if ( typeVal != null ) {
                this.setType(PropertyType.valueOf(typeVal.toUpperCase()));				
			}
			final JsonValue rangeVal = this.getAttributes().remove(InternalConstants.KEY_RANGE);
			if ( rangeVal != null ) {
				final Range range = new Range();
				range.fromJSONObject(rangeVal.asJsonObject());
				this.setRange(range);
			}
			final JsonValue incs = this.getAttributes().remove(InternalConstants.KEY_INCLUDES);
			if ( incs != null ) {
				final List<String> list = new ArrayList<>();
				for(final JsonValue innerVal : incs.asJsonArray()) {
                    list.add(getString(innerVal));
                }
                this.setIncludes(list.toArray(new String[list.size()]));
			}
			final JsonValue excs = this.getAttributes().remove(InternalConstants.KEY_EXCLUDES);
			if ( excs != null ) {
				final List<String> list = new ArrayList<>();
				for(final JsonValue innerVal : excs.asJsonArray()) {
                    list.add(getString(innerVal));
                }
                this.setExcludes(list.toArray(new String[list.size()]));
			}
			final JsonValue opts = this.getAttributes().remove(InternalConstants.KEY_OPTIONS);
			if ( opts != null ) {
				final List<Option> list = new ArrayList<>();
				for(final JsonValue innerVal : opts.asJsonArray()) {
					final Option o = new Option();
					o.fromJSONObject(innerVal.asJsonObject());
					list.add(o);
                }
				this.setOptions(list);
			}
			this.setRegex(this.getString(InternalConstants.KEY_REGEX));
 		} catch (final JsonException | IllegalArgumentException e) {
            throw new IOException(e);
        }
	}
	
    /**
     * Convert this object into JSON
     *
     * @return The json object builder
     * @throws IOException If generating the JSON fails
     */
    @Override
    JsonObjectBuilder createJson() throws IOException {
		final JsonObjectBuilder objectBuilder = super.createJson();

		if ( this.getType() != null && this.getType() != PropertyType.STRING ) {
			this.setString(objectBuilder, InternalConstants.KEY_TYPE, this.getType().name());
	    }
		if ( this.getCardinality() != 1 ) {
			objectBuilder.add(InternalConstants.KEY_CARDINALITY, this.getCardinality());
		}
		if ( this.isRequired() ) {
			objectBuilder.add(InternalConstants.KEY_REQUIRED, this.isRequired());
		}
	    this.setString(objectBuilder, InternalConstants.KEY_VARIABLE, this.getVariable());
		
		if ( this.range != null ) {
			objectBuilder.add(InternalConstants.KEY_RANGE, this.range.toJSONObject());
		}
		if ( this.includes != null && this.includes.length > 0 ) {
			final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for(final String v : this.includes) {
				arrayBuilder.add(v);
			}
			objectBuilder.add(InternalConstants.KEY_INCLUDES, arrayBuilder);
		}
		if ( this.excludes != null && this.excludes.length > 0 ) {
			final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for(final String v : this.excludes) {
				arrayBuilder.add(v);
			}
			objectBuilder.add(InternalConstants.KEY_EXCLUDES, arrayBuilder);
		}
		if ( this.options != null && !this.options.isEmpty()) {
			final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for(final Option o : this.options) {
				arrayBuilder.add(o.toJSONObject());
			}
			objectBuilder.add(InternalConstants.KEY_OPTIONS, arrayBuilder);
		}
		this.setString(objectBuilder, InternalConstants.KEY_REGEX, this.getRegex());
		
		return objectBuilder;
	}

    /**
	 * Get the property type
	 * @return the type
	 */
	public PropertyType getType() {
		return type;
	}

	/**
	 * Set the property type
	 * @param type the type to set
	 */
	public void setType(final PropertyType type) {
		this.type = type == null ? PropertyType.STRING : type;
	}

	/**
	 * Get the cardinality
	 * @return the cardinality
	 */
	public int getCardinality() {
		return cardinality;
	}

	/**
	 * Set the cardinality
	 * @param cardinality the cardinality to set
	 */
	public void setCardinality(final int cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Get the variable
	 * @return the variable or {@code null}
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * Set the variable
	 * @param variable the variable to set
	 */
	public void setVariable(final String variable) {
		this.variable = variable;
	}

	/**
	 * Get the range
	 * @return the range or {@code null}
	 */
	public Range getRange() {
		return range;
	}

	/**
	 * Set the range
	 * @param range the range to set
	 */
	public void setRange(final Range range) {
		this.range = range;
	}

	/**
	 * Get the includes
	 * @return the includes or {@code null}
	 */
	public String[] getIncludes() {
		return includes;
	}

	/**
	 * Set the includes
	 * @param includes the includes to set
	 */
	public void setIncludes(final String[] includes) {
		this.includes = includes;
	}

	/**
	 * Get the excludes
	 * @return the excludes or {@code null}
	 */
	public String[] getExcludes() {
		return excludes;
	}

	/**
	 * Set the excludes
	 * @param excludes the excludes to set
	 */
	public void setExcludes(final String[] excludes) {
		this.excludes = excludes;
	}

	/**
	 * Get the list of options
	 * @return the options or {@code null}
	 */
	public List<Option> getOptions() {
		return options;
	}

	/**
	 * Set the list of options
	 * @param options the options to set
	 */
	public void setOptions(final List<Option> options) {
		this.options = options;
	}

	/**
	 * Get the regex
	 * @return the regex or {@code null}
	 */
	public String getRegex() {
		return pattern == null ? null : pattern.pattern();
	}

	/**
	 * Set the regex
	 * @param regex the regex to set
     * @throws IllegalArgumentException If the pattern is not valid
	 */
	public void setRegex(final String regex) {
        if ( regex == null ) {
            this.pattern = null;
        } else {
           this.pattern = Pattern.compile(regex);
        }
	}

    /**
     * Get the regex pattern
     * @return The pattern or {@code null}
     */
    public Pattern getRegexPattern() {
        return this.pattern;
    }

	/**
	 * Is this property required?
	 * @return {@code true} if it is required
	 */
	public boolean isRequired() {
		return this.required;
	}

	/**
	 * Set whether this property is required
	 * @param flag The new value
	 */
	public void setRequired(final boolean flag) {
		this.required = flag;
	}
}
