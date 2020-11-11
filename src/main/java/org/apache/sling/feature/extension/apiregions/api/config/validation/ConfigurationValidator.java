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
package org.apache.sling.feature.extension.apiregions.api.config.validation;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.sling.feature.Configuration;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurableEntity;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FactoryConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.PropertyDescription;
import org.osgi.framework.Constants;

/**
 * Validator to validate a configuration
 */
public class ConfigurationValidator {
    
    /**
     * List of properties which are always allowed
     */
    public static final List<String> ALLOWED_PROPERTIES = Arrays.asList(Constants.SERVICE_DESCRIPTION,
        Constants.SERVICE_VENDOR,
        Constants.SERVICE_RANKING);


    private final PropertyValidator propertyValidator = new PropertyValidator();

    /**
     * Validate a configuration
     * @param The configuration description 
     * @param config The OSGi configuration
     * @return The result
     */
    public ConfigurationValidationResult validate(final ConfigurableEntity desc, final Configuration config) {
        final ConfigurationValidationResult result = new ConfigurationValidationResult();
        if ( config.isFactoryConfiguration() ) {
            if ( !(desc instanceof FactoryConfigurationDescription) ) {
                result.getGlobalErrors().add("Factory configuration cannot be validated against non factory configuration description");
            } else {
                final FactoryConfigurationDescription fDesc = (FactoryConfigurationDescription)desc;
                if ( fDesc.getOperations().isEmpty() ) {
                    result.getGlobalErrors().add("Factory configuration not allowed");
                } else if ( fDesc.getInternalNames().contains(config.getName())) {
                    result.getGlobalErrors().add("Factory configuration with name " + config.getName() + " not allowed");
                } else {
                    validateProperties(desc, config, result.getPropertyResults());
                }
            }
        } else {
            if ( !(desc instanceof ConfigurationDescription) ) {
                result.getGlobalErrors().add("Configuration cannot be validated against factory configuration description");
            } else {
                validateProperties(desc, config, result.getPropertyResults());
            }
        }

        if ( desc.getDeprecated() != null ) {
            result.getWarnings().add(desc.getDeprecated());
        }
        return result;
    }

    void validateProperties(final ConfigurableEntity desc, 
            final Configuration configuration, 
            final Map<String, PropertyValidationResult> results) {
        final Dictionary<String, Object> properties = configuration.getConfigurationProperties();
        for(final Map.Entry<String, PropertyDescription> propEntry : desc.getPropertyDescriptions().entrySet()) {
            final Object value = properties.get(propEntry.getKey());
            final PropertyValidationResult result = propertyValidator.validate(propEntry.getValue(), value);
            results.put(propEntry.getKey(), result);
        }
        final Enumeration<String> keyEnum = properties.keys();
        while ( keyEnum.hasMoreElements() ) {
            final String propName = keyEnum.nextElement();
            if ( !desc.getPropertyDescriptions().containsKey(propName) ) {
                final PropertyValidationResult result = new PropertyValidationResult();
                results.put(propName, result);
                if ( Constants.SERVICE_RANKING.equals(propName) ) {
                    final Object value = properties.get(propName);
                    if ( !(value instanceof Integer) ) {
                        result.getErrors().add("service.ranking must be of type Integer");
                    }    
                } else if ( !ALLOWED_PROPERTIES.contains(propName) ) {
                    result.getErrors().add("Property is not allowed");

                }
            }
        }
    }
}