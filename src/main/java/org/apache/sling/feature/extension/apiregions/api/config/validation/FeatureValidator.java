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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Configuration;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.FeatureProvider;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationApi;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FactoryConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FrameworkPropertyDescription;
import org.apache.sling.feature.extension.apiregions.api.config.Operation;
import org.apache.sling.feature.extension.apiregions.api.config.Region;

/**
 * Validator to validate a feature
 */
public class FeatureValidator {
    
    private final ConfigurationValidator configurationValidator = new ConfigurationValidator();

    private final PropertyValidator propertyValidator = new PropertyValidator();

    private FeatureProvider featureProvider;

    /**
     * Get the current feature provider
     * @return the feature provider or {@code null}
     */
    public FeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    /**
     * Set the feature provider
     * @param provider the feature provider to set
     */
    public void setFeatureProvider(final FeatureProvider provider) {
        this.featureProvider = provider;
    }

    /**
     * Validate the feature against its configuration API
     * @param feature The feature
     * @return A {@code FeatureValidationResult}
     * @throws IllegalArgumentException If api is not available
     * @since 1.1
     */
    public FeatureValidationResult validate(final Feature feature) {
        return validate(feature, ConfigurationApi.getConfigurationApi(feature));
    }

    /**
     * Validate the feature against the configuration API
     * @param feature The feature
     * @param api The configuration API
     * @return A {@code FeatureValidationResult}
     * @throws IllegalArgumentException If api is {@code null}
     */
    public FeatureValidationResult validate(final Feature feature, final ConfigurationApi api) {
        final FeatureValidationResult result = new FeatureValidationResult();
        if ( api == null ) {
            throw new IllegalArgumentException();
        }

        final Map<ArtifactId, Region> cache = new HashMap<>(api.getFeatureToRegionCache());
        cache.put(feature.getId(), api.detectRegion());

        for(final Configuration config : feature.getConfigurations()) {
            final RegionInfo regionInfo = getRegionInfo(feature, config, cache);

            if ( regionInfo == null ) {
                final ConfigurationValidationResult cvr = new ConfigurationValidationResult();
                cvr.getErrors().add("Unable to properly validate configuration, region info cannot be determined");
                result.getConfigurationResults().put(config.getPid(), cvr);
            } else {
                if ( config.isFactoryConfiguration() ) {
                    final FactoryConfigurationDescription desc = api.getFactoryConfigurationDescriptions().get(config.getFactoryPid());
                    if ( desc != null ) {
                        final ConfigurationValidationResult r = configurationValidator.validate(config, desc, regionInfo.region, api.getMode());
                        result.getConfigurationResults().put(config.getPid(), r);
                        if ( regionInfo.region != Region.INTERNAL ) {
                            if ( desc.getOperations().isEmpty() ) {
                                r.getErrors().add("No operations allowed for factory configuration");
                            } else {
                                if ( regionInfo.isUpdate && !desc.getOperations().contains(Operation.UPDATE)) {
                                    r.getErrors().add("Updating of factory configuration is not allowed");
                                } else if ( !regionInfo.isUpdate && !desc.getOperations().contains(Operation.CREATE)) {
                                    r.getErrors().add("Creation of factory configuration is not allowed");
                                }
                            }
                            if ( desc.getInternalNames().contains(config.getName())) {
                                r.getErrors().add("Factory configuration with name is not allowed");
                            }
                        }                        

                    } else if ( regionInfo.region != Region.INTERNAL && api.getInternalFactoryConfigurations().contains(config.getFactoryPid())) {
                        final ConfigurationValidationResult cvr = new ConfigurationValidationResult();
                        cvr.getErrors().add("Factory configuration is not allowed");
                        result.getConfigurationResults().put(config.getPid(), cvr);
                    }
                } else {
                    final ConfigurationDescription desc = api.getConfigurationDescriptions().get(config.getPid());
                    if ( desc != null ) {
                        final ConfigurationValidationResult r = configurationValidator.validate(config, desc, regionInfo.region, api.getMode());
                        result.getConfigurationResults().put(config.getPid(), r);
                    } else if ( regionInfo.region!= Region.INTERNAL && api.getInternalConfigurations().contains(config.getPid())) {
                        final ConfigurationValidationResult cvr = new ConfigurationValidationResult();
                        cvr.getErrors().add("Configuration is not allowed");
                        result.getConfigurationResults().put(config.getPid(), cvr);
                    } 
                }    
            }

            // make sure a result exists
            result.getConfigurationResults().computeIfAbsent(config.getPid(), id -> new ConfigurationValidationResult());
        }

        for(final String frameworkProperty : feature.getFrameworkProperties().keySet()) {
            final RegionInfo regionInfo = getRegionInfo(feature, frameworkProperty, cache);
            if ( regionInfo == null ) {
                final PropertyValidationResult pvr = new PropertyValidationResult();
                pvr.getErrors().add("Unable to properly validate framework property, region info cannot be determined");
                result.getFrameworkPropertyResults().put(frameworkProperty, pvr);
            } else {
                final FrameworkPropertyDescription fpd = api.getFrameworkPropertyDescriptions().get(frameworkProperty);
                if ( fpd != null ) {
                    final PropertyValidationResult pvr = propertyValidator.validate(feature.getFrameworkProperties().get(frameworkProperty), fpd, api.getMode());
                    result.getFrameworkPropertyResults().put(frameworkProperty, pvr);
                } else if ( regionInfo.region != Region.INTERNAL && api.getInternalFrameworkProperties().contains(frameworkProperty) ) {
                    final PropertyValidationResult pvr = new PropertyValidationResult();
                    pvr.getErrors().add("Framework property is not allowed");
                    result.getFrameworkPropertyResults().put(frameworkProperty, pvr);
                }
            } 
            // make sure a result exists
            result.getFrameworkPropertyResults().computeIfAbsent(frameworkProperty, id -> new PropertyValidationResult());
        }

        return result;
    }

    Region getConfigurationApiRegion(final ArtifactId id, final Map<ArtifactId, Region> cache) {
        Region result = cache.get(id);
        if ( result == null ) {
            result = Region.GLOBAL;
            cache.put(id, result);
        }
        return result;
    }

    static final class RegionInfo {
        
        public Region region;

        public boolean isUpdate;
    }

    RegionInfo getRegionInfo(final Feature feature, final Configuration cfg, final Map<ArtifactId, Region> cache) {
        final RegionInfo result = new RegionInfo();
        
        final List<ArtifactId> list = cfg.getFeatureOrigins();
        if ( !list.isEmpty() ) {
            boolean global = false;
            for(final ArtifactId id : list) {
                final Region region = getConfigurationApiRegion(id, cache);
                if ( region == null ) {
                    return null;
                }
                if ( region == Region.GLOBAL ) {
                    global = true;
                    break;
                }
            }
            result.region = global ? Region.GLOBAL : Region.INTERNAL;
            result.isUpdate = list.size() > 1;
        } else {
            final Region region = getConfigurationApiRegion(feature.getId(), cache);
            result.region = region == Region.INTERNAL ? Region.INTERNAL : Region.GLOBAL;
            result.isUpdate = false;
        }
        return result;
    }

    RegionInfo getRegionInfo(final Feature feature, final String frameworkProperty, final Map<ArtifactId, Region> cache) {
        final List<ArtifactId> list = feature.getFeatureOrigins(feature.getFrameworkPropertyMetadata(frameworkProperty));
        boolean global = false;
        for(final ArtifactId id : list) {
            final Region region = getConfigurationApiRegion(id, cache);
            if ( region == null ) {
                return null;
            }
            if ( region == Region.GLOBAL ) {
                global = true;
                break;
            }
    }
        final RegionInfo result = new RegionInfo();
        result.region = global ? Region.GLOBAL : Region.INTERNAL;
        result.isUpdate = list.size() > 1;

        return result;
    }
}
