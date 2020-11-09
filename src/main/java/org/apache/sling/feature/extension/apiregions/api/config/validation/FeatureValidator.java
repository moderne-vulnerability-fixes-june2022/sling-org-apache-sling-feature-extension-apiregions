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

import org.apache.sling.feature.Configuration;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationApi;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FactoryConfigurationDescription;

/**
 * Validator to validate a feature
 */
public class FeatureValidator {
    
    private final ConfigurationValidator configurationValidator = new ConfigurationValidator();

    /**
     * Validate the feature against the configuration API
     * @param api The configuration API
     * @param feature The feature
     * @return A {@code FeatureValidationResult}
     */
    public FeatureValidationResult validate(final ConfigurationApi api, final Feature feature) {
        final FeatureValidationResult result = new FeatureValidationResult();

        for(final Configuration config : feature.getConfigurations()) {
            if ( config.isFactoryConfiguration() ) {
                final FactoryConfigurationDescription desc = api.getFactoryConfigurationDescriptions().get(config.getFactoryPid());
                if ( desc != null ) {
                    final ConfigurationValidationResult r = configurationValidator.validate(desc, config);
                    if ( !r.isValid()) {
                        result.getConfigurationErrors().put(config.getPid(), r);
                    }
                } else if ( api.getInternalFactoryConfigurations().contains(config.getFactoryPid())) {
                    final ConfigurationValidationResult cvr = new ConfigurationValidationResult();
                    cvr.getGlobalErrors().add("Factory configuration is not allowed");
                    result.getConfigurationErrors().put(config.getPid(), cvr);
                }
            } else {
                final ConfigurationDescription desc = api.getConfigurationDescriptions().get(config.getPid());
                if ( desc != null ) {
                    final ConfigurationValidationResult r = configurationValidator.validate(desc, config);
                    if ( !r.isValid()) {
                        result.getConfigurationErrors().put(config.getPid(), r);
                    }
                } else if ( api.getInternalConfigurations().contains(config.getPid())) {
                    final ConfigurationValidationResult cvr = new ConfigurationValidationResult();
                    cvr.getGlobalErrors().add("Configuration is not allowed");
                    result.getConfigurationErrors().put(config.getPid(), cvr);
                }
            }
        }
        return result;
    }
}