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
package org.apache.sling.feature.extension.apiregions;

import java.util.Map;

import org.apache.sling.feature.Extension;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.HandlerContext;
import org.apache.sling.feature.builder.MergeHandler;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationApi;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FactoryConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FrameworkPropertyDescription;
import org.apache.sling.feature.extension.apiregions.api.config.Region;

/**
 * Merge the configuration api extension
 */
public class ConfigurationApiMergeHandler implements MergeHandler {

    @Override
    public boolean canMerge(final Extension extension) {
        return ConfigurationApi.EXTENSION_NAME.equals(extension.getName());
    }

    @Override
    public void merge(final HandlerContext context, 
        final Feature targetFeature, 
        final Feature sourceFeature, 
        final Extension targetExtension, 
        final Extension sourceExtension) {

        if ( targetExtension == null ) {
            // no target available yet, just copy source and update cache
            final ConfigurationApi sourceApi = ConfigurationApi.getConfigurationApi(sourceExtension);
            sourceApi.getFeatureToRegionCache().put(sourceFeature.getId(), sourceApi.detectRegion());

            ConfigurationApi.setConfigurationApi(targetFeature, sourceApi);
        } else {
            final ConfigurationApi sourceApi = ConfigurationApi.getConfigurationApi(sourceExtension);
            final ConfigurationApi targetApi = ConfigurationApi.getConfigurationApi(targetExtension);

            // region merging
            if ( context.isInitialMerge() ) {
                targetApi.setRegion(sourceApi.getRegion());
                targetApi.setMode(sourceApi.getMode());
            } else {
                // region merging is different for prototypes
                if ( sourceApi.getRegion() != targetApi.getRegion() ) {
                    if ( context.isPrototypeMerge() ) {
                        if ( sourceApi.getRegion() != null ) {
                            targetApi.setRegion(sourceApi.getRegion());
                        }
                    } else {                    
                        targetApi.setRegion(Region.GLOBAL);
                    }
                }
                if ( targetApi.getMode().ordinal() > sourceApi.getMode().ordinal() ) {
                    targetApi.setMode(sourceApi.getMode());
                }
            }

            // merge - but throw on duplicates
            for(final Map.Entry<String, ConfigurationDescription> entry : sourceApi.getConfigurationDescriptions().entrySet()) {
                if ( targetApi.getConfigurationDescriptions().containsKey(entry.getKey())) {
                    throw new IllegalStateException("Duplicate configuration description " + entry.getKey());
                }
                targetApi.getConfigurationDescriptions().put(entry.getKey(), entry.getValue());
            }
            for(final Map.Entry<String, FactoryConfigurationDescription> entry : sourceApi.getFactoryConfigurationDescriptions().entrySet()) {
                if ( targetApi.getFactoryConfigurationDescriptions().containsKey(entry.getKey())) {
                    throw new IllegalStateException("Duplicate factory configuration description " + entry.getKey());
                }
                targetApi.getFactoryConfigurationDescriptions().put(entry.getKey(), entry.getValue());
            }
            for(final Map.Entry<String, FrameworkPropertyDescription> entry : sourceApi.getFrameworkPropertyDescriptions().entrySet()) {
                if ( targetApi.getFrameworkPropertyDescriptions().containsKey(entry.getKey())) {
                    throw new IllegalStateException("Duplicate framework property description " + entry.getKey());
                }
                targetApi.getFrameworkPropertyDescriptions().put(entry.getKey(), entry.getValue());
            }
            targetApi.getInternalConfigurations().addAll(sourceApi.getInternalConfigurations());
            targetApi.getInternalFactoryConfigurations().addAll(sourceApi.getInternalFactoryConfigurations());
            targetApi.getInternalFrameworkProperties().addAll(sourceApi.getInternalFrameworkProperties());

            // update cache
            if ( !context.isPrototypeMerge() ) {
                targetApi.getFeatureToRegionCache().put(sourceFeature.getId(), sourceApi.detectRegion());
                targetApi.getFeatureToRegionCache().putAll(sourceApi.getFeatureToRegionCache());
            }
            
            ConfigurationApi.setConfigurationApi(targetFeature, targetApi);
        }
    }
}
