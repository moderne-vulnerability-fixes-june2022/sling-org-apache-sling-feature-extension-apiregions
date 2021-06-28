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

import java.util.ArrayList;
import java.util.Collections;
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

    private boolean liveValues = false;

    final Map<ArtifactId, Region> cache = new HashMap<>();

    /**
     * Create a new feature validator
     */
    public FeatureValidator() {
        this.configurationValidator.setCache(cache);
    }

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
     * Are live values validated?
     * @return {@code true} if live values are validated
     * @since 1.4
     */
    public boolean isLiveValues() {
        return liveValues;
    }

    /**
     * Set whether live values are validated.
     * @param value Flag for validating live values
     * @since 1.4
     */
    public void setLiveValues(final boolean value) {
        this.liveValues = value;
        this.configurationValidator.setLiveValues(value);
        this.propertyValidator.setLiveValues(value);
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
        cache.putAll(api.getFeatureToRegionCache());
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
                                ConfigurationValidator.setResult(r, api.getMode(), "No operations allowed for factory configuration");
                            } else {
                                if ( regionInfo.isUpdate && !desc.getOperations().contains(Operation.UPDATE)) {
                                    ConfigurationValidator.setResult(r, api.getMode(), "Updating of factory configuration is not allowed");
                                } else if ( !regionInfo.isUpdate && !desc.getOperations().contains(Operation.CREATE)) {
                                    ConfigurationValidator.setResult(r, api.getMode(), "Creation of factory configuration is not allowed");
                                }
                            }
                            if ( desc.getInternalNames().contains(config.getName())) {
                                ConfigurationValidator.setResult(r, api.getMode(), "Factory configuration with name is not allowed");
                            }
                        }                        

                    } else if ( regionInfo.region != Region.INTERNAL && api.getInternalFactoryConfigurations().contains(config.getFactoryPid())) {
                        final ConfigurationValidationResult cvr = new ConfigurationValidationResult();
                        ConfigurationValidator.setResult(cvr, api.getMode(), "Factory configuration is not allowed");
                        result.getConfigurationResults().put(config.getPid(), cvr);
                    }
                } else {
                    final ConfigurationDescription desc = api.getConfigurationDescriptions().get(config.getPid());
                    if ( desc != null ) {
                        final ConfigurationValidationResult r = configurationValidator.validate(config, desc, regionInfo.region, api.getMode());
                        result.getConfigurationResults().put(config.getPid(), r);
                    } else if ( regionInfo.region!= Region.INTERNAL && api.getInternalConfigurations().contains(config.getPid())) {
                        final ConfigurationValidationResult cvr = new ConfigurationValidationResult();
                        ConfigurationValidator.setResult(cvr, api.getMode(), "Configuration is not allowed");
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
                    PropertyValidator.setResult(pvr, null, api.getMode(), "Framework property is not allowed");
                    result.getFrameworkPropertyResults().put(frameworkProperty, pvr);
                }
            } 
            // make sure a result exists
            result.getFrameworkPropertyResults().computeIfAbsent(frameworkProperty, id -> new PropertyValidationResult());
        }

        return result;
    }

    /**
     * Apply default values from the result of a validation run.
     * Defaults should be applied, if configuration properties are invalid and the validation mode
     * for such a properties is definitive.
     * @param feature The feature containing the configurations
     * @param result The result
     * @return {@code true} if a default value has been applied (the feature has been changed)
     * @since 1.2
     */
    public boolean applyDefaultValues(final Feature feature, final FeatureValidationResult result) {
        boolean changed = false;

        for(final Map.Entry<String, ConfigurationValidationResult> entry : result.getConfigurationResults().entrySet()) {
            if ( entry.getValue().isUseDefaultValue() ) {
                final Configuration cfg = feature.getConfigurations().getConfiguration(entry.getKey());
                if ( cfg != null ) {
                    final List<String> keys = new ArrayList<>(Collections.list(cfg.getConfigurationProperties().keys()));
                    for(final String k : keys ) {
                        cfg.getProperties().remove(k);
                    }
                }
            }
            for(final Map.Entry<String, PropertyValidationResult> propEntry : entry.getValue().getPropertyResults().entrySet()) {
                if ( propEntry.getValue().isUseDefaultValue() ) {
                    final Configuration cfg = feature.getConfigurations().getConfiguration(entry.getKey());
                    if ( cfg != null ) {
                        if ( propEntry.getValue().getDefaultValue() == null ) {
                            cfg.getProperties().remove(propEntry.getKey());
                        } else {
                            cfg.getProperties().put(propEntry.getKey(), propEntry.getValue().getDefaultValue());
                        }
                        changed = true;
                    }
                }
            }
        }

        for(final Map.Entry<String, PropertyValidationResult> propEntry : result.getFrameworkPropertyResults().entrySet()) {
            if ( propEntry.getValue().isUseDefaultValue() ) {
                if ( propEntry.getValue().getDefaultValue() == null ) {
                    feature.getFrameworkProperties().remove(propEntry.getKey());
                } else {
                    feature.getFrameworkProperties().put(propEntry.getKey(), propEntry.getValue().getDefaultValue().toString());
                }
                changed = true;
            }
        }

        return changed;
    }

    static Region getConfigurationApiRegion(final ArtifactId id, final Map<ArtifactId, Region> cache) {
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

    static Region getRegionInfo(final Region cfgRegion, final Configuration cfg, final String propertyName, final Map<ArtifactId, Region> cache) {
        final List<ArtifactId> list = cfg.getFeatureOrigins(propertyName);
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
            return global ? Region.GLOBAL : Region.INTERNAL;
        }
        return cfgRegion;
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
