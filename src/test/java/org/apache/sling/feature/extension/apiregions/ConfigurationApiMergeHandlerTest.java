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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.Prototype;
import org.apache.sling.feature.builder.BuilderContext;
import org.apache.sling.feature.builder.FeatureBuilder;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationApi;
import org.apache.sling.feature.extension.apiregions.api.config.Region;
import org.junit.Test;

public class ConfigurationApiMergeHandlerTest {

    @Test public void testPrototypeRegionMerge() {
        final Feature prototype = new Feature(ArtifactId.parse("g:p:1"));
        final ConfigurationApi prototypeApi = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);

        // always return prototype
        final BuilderContext context = new BuilderContext(id -> prototype);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());
        
        final Feature feature = new Feature(ArtifactId.parse("g:f:1"));
        feature.setPrototype(new Prototype(prototype.getId()));
        final ConfigurationApi featureApi = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(feature, featureApi);

        // no region
        Feature result = FeatureBuilder.assemble(feature, context);
        ConfigurationApi api = ConfigurationApi.getConfigurationApi(result);
        assertNotNull(api);
        assertNull(api.getRegion());

        // prototype has region
        prototypeApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());

        prototypeApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        // feature has region
        prototypeApi.setRegion(null);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());

        featureApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        // both have region
        prototypeApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());

        prototypeApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());

        prototypeApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        prototypeApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
    }
 
    @Test public void testRegionMerge() {
        // always return prototype
        final BuilderContext context = new BuilderContext(id -> null);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());

        final Feature featureA = new Feature(ArtifactId.parse("g:a:1"));
        final ConfigurationApi apiA = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        
        final Feature featureB = new Feature(ArtifactId.parse("g:b:1"));
        final ConfigurationApi apiB = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(featureB, apiB);

        // no region
        final ArtifactId id = ArtifactId.parse("g:m:1");
        Feature result = FeatureBuilder.assemble(id, context, featureA, featureB);
        ConfigurationApi api = ConfigurationApi.getConfigurationApi(result);
        assertNotNull(api);
        assertNull(api.getRegion());

        // only A has region
        apiA.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        apiA.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        // only B has region
        apiA.setRegion(null);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        apiB.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        // both have region
        apiA.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());

        apiA.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        apiA.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());

        apiA.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
    }
}