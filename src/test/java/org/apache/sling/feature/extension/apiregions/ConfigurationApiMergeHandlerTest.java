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
import static org.junit.Assert.assertTrue;

import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.Prototype;
import org.apache.sling.feature.builder.BuilderContext;
import org.apache.sling.feature.builder.FeatureBuilder;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationApi;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FactoryConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FrameworkPropertyDescription;
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
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(prototype.getId()));

        // prototype has region
        prototypeApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(prototype.getId()));

        prototypeApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(prototype.getId()));

        // feature has region
        prototypeApi.setRegion(null);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(prototype.getId()));

        featureApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(prototype.getId()));

        // both have region
        prototypeApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(prototype.getId()));

        prototypeApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(prototype.getId()));

        prototypeApi.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(prototype.getId()));

        prototypeApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(prototype, prototypeApi);
        featureApi.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(feature, featureApi);
        result = FeatureBuilder.assemble(feature, context);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(1, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(prototype.getId()));
    }
 
    @Test public void testRegionMerge() {
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
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureB.getId()));

        // only A has region
        apiA.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureB.getId()));

        apiA.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureB.getId()));

        // only B has region
        apiA.setRegion(null);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureB.getId()));

        apiB.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureB.getId()));

        // both have region
        apiA.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.INTERNAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureB.getId()));

        apiA.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureB.getId()));

        apiA.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureB.getId()));

        apiA.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        apiB.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);
        result = FeatureBuilder.assemble(id, context, featureA, featureB);
        api = ConfigurationApi.getConfigurationApi(result);
        assertEquals(Region.GLOBAL, api.getRegion());
        assertEquals(2, api.getFeatureToRegionCache().size());
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureB.getId()));
    }

    @Test public void testConfigurationApiMergeDifferentConfig() {
        final BuilderContext context = new BuilderContext(id -> null);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());

        final Feature featureA = new Feature(ArtifactId.parse("g:a:1"));
        final ConfigurationApi apiA = new ConfigurationApi();
        apiA.getConfigurationDescriptions().put("a", new ConfigurationDescription());
        apiA.getFactoryConfigurationDescriptions().put("fa", new FactoryConfigurationDescription());
        apiA.getFrameworkPropertyDescriptions().put("pa", new FrameworkPropertyDescription());
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        
        final Feature featureB = new Feature(ArtifactId.parse("g:b:1"));
        final ConfigurationApi apiB = new ConfigurationApi();
        apiB.getConfigurationDescriptions().put("b", new ConfigurationDescription());
        apiB.getFactoryConfigurationDescriptions().put("fb", new FactoryConfigurationDescription());
        apiB.getFrameworkPropertyDescriptions().put("pb", new FrameworkPropertyDescription());
        ConfigurationApi.setConfigurationApi(featureB, apiB);

        final ArtifactId id = ArtifactId.parse("g:m:1");
        Feature result = FeatureBuilder.assemble(id, context, featureA, featureB);
        ConfigurationApi api = ConfigurationApi.getConfigurationApi(result);
        assertNotNull(api);

        assertEquals(2, api.getConfigurationDescriptions().size());
        assertNotNull(api.getConfigurationDescriptions().get("a"));
        assertNotNull(api.getConfigurationDescriptions().get("b"));

        assertEquals(2, api.getFactoryConfigurationDescriptions().size());
        assertNotNull(api.getFactoryConfigurationDescriptions().get("fa"));
        assertNotNull(api.getFactoryConfigurationDescriptions().get("fb"));

        assertEquals(2, api.getFrameworkPropertyDescriptions().size());
        assertNotNull(api.getFrameworkPropertyDescriptions().get("pa"));
        assertNotNull(api.getFrameworkPropertyDescriptions().get("pb"));
    }

    @Test(expected = IllegalStateException.class)
    public void testConfigurationApiMergeSameConfigurations() {
        final BuilderContext context = new BuilderContext(id -> null);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());

        final Feature featureA = new Feature(ArtifactId.parse("g:a:1"));
        final ConfigurationApi apiA = new ConfigurationApi();
        apiA.getConfigurationDescriptions().put("a", new ConfigurationDescription());
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        
        final Feature featureB = new Feature(ArtifactId.parse("g:b:1"));
        final ConfigurationApi apiB = new ConfigurationApi();
        apiB.getConfigurationDescriptions().put("a", new ConfigurationDescription());
        ConfigurationApi.setConfigurationApi(featureB, apiB);

        final ArtifactId id = ArtifactId.parse("g:m:1");
        FeatureBuilder.assemble(id, context, featureA, featureB);
    }

    @Test(expected = IllegalStateException.class)
    public void testConfigurationApiMergeSameFactoryConfigurations() {
        final BuilderContext context = new BuilderContext(id -> null);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());

        final Feature featureA = new Feature(ArtifactId.parse("g:a:1"));
        final ConfigurationApi apiA = new ConfigurationApi();
        apiA.getFactoryConfigurationDescriptions().put("fa", new FactoryConfigurationDescription());
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        
        final Feature featureB = new Feature(ArtifactId.parse("g:b:1"));
        final ConfigurationApi apiB = new ConfigurationApi();
        apiB.getFactoryConfigurationDescriptions().put("fa", new FactoryConfigurationDescription());
        ConfigurationApi.setConfigurationApi(featureB, apiB);

        final ArtifactId id = ArtifactId.parse("g:m:1");
        FeatureBuilder.assemble(id, context, featureA, featureB);
    }

    @Test(expected = IllegalStateException.class)
    public void testConfigurationApiMergeSameFrameworkProperties() {
        final BuilderContext context = new BuilderContext(id -> null);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());

        final Feature featureA = new Feature(ArtifactId.parse("g:a:1"));
        final ConfigurationApi apiA = new ConfigurationApi();
        apiA.getFrameworkPropertyDescriptions().put("pa", new FrameworkPropertyDescription());
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        
        final Feature featureB = new Feature(ArtifactId.parse("g:b:1"));
        final ConfigurationApi apiB = new ConfigurationApi();
        apiB.getFrameworkPropertyDescriptions().put("pa", new FrameworkPropertyDescription());
        ConfigurationApi.setConfigurationApi(featureB, apiB);

        final ArtifactId id = ArtifactId.parse("g:m:1");
        FeatureBuilder.assemble(id, context, featureA, featureB);
    }

    @Test public void testConfigurationApiMergeInternalNames() {
        final BuilderContext context = new BuilderContext(id -> null);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());

        final Feature featureA = new Feature(ArtifactId.parse("g:a:1"));
        final ConfigurationApi apiA = new ConfigurationApi();
        apiA.getInternalConfigurations().add("a");
        apiA.getInternalFactoryConfigurations().add("fa");
        apiA.getInternalFrameworkProperties().add("pa");

        apiA.getInternalConfigurations().add("c");
        apiA.getInternalFactoryConfigurations().add("fc");
        apiA.getInternalFrameworkProperties().add("pc");
        ConfigurationApi.setConfigurationApi(featureA, apiA);
        
        final Feature featureB = new Feature(ArtifactId.parse("g:b:1"));
        final ConfigurationApi apiB = new ConfigurationApi();
        apiB.getInternalConfigurations().add("b");
        apiB.getInternalFactoryConfigurations().add("fb");
        apiB.getInternalFrameworkProperties().add("pb");

        apiB.getInternalConfigurations().add("c");
        apiB.getInternalFactoryConfigurations().add("fc");
        apiB.getInternalFrameworkProperties().add("pc");
        ConfigurationApi.setConfigurationApi(featureB, apiB);

        final ArtifactId id = ArtifactId.parse("g:m:1");
        Feature result = FeatureBuilder.assemble(id, context, featureA, featureB);
        ConfigurationApi api = ConfigurationApi.getConfigurationApi(result);
        assertNotNull(api);

        assertEquals(3, api.getInternalConfigurations().size());
        assertTrue(api.getInternalConfigurations().contains("a"));
        assertTrue(api.getInternalConfigurations().contains("b"));
        assertTrue(api.getInternalConfigurations().contains("c"));

        assertEquals(3, api.getInternalFactoryConfigurations().size());
        assertTrue(api.getInternalFactoryConfigurations().contains("fa"));
        assertTrue(api.getInternalFactoryConfigurations().contains("fb"));
        assertTrue(api.getInternalFactoryConfigurations().contains("fc"));

        assertEquals(3, api.getInternalFrameworkProperties().size());
        assertTrue(api.getInternalFrameworkProperties().contains("pa"));
        assertTrue(api.getInternalFrameworkProperties().contains("pb"));
        assertTrue(api.getInternalFrameworkProperties().contains("pc"));
    }

    @Test public void testConfigurationApiMergeRegionCache() {
        final BuilderContext context = new BuilderContext(id -> null);
        context.addMergeExtensions(new ConfigurationApiMergeHandler());

        final Feature featureA = new Feature(ArtifactId.parse("g:a:1"));
        final ConfigurationApi apiA = new ConfigurationApi();
        apiA.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureA, apiA);

        final Feature featureB = new Feature(ArtifactId.parse("g:b:1"));
        final ConfigurationApi apiB = new ConfigurationApi();
        apiB.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(featureB, apiB);


        final Feature featureC = new Feature(ArtifactId.parse("g:c:1"));
        final ConfigurationApi apiC = new ConfigurationApi();
        apiC.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureC, apiC);

        final Feature featureD = new Feature(ArtifactId.parse("g:d:1"));
        final ConfigurationApi apiD = new ConfigurationApi();
        apiD.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(featureD, apiD);

        final ArtifactId idIntermediate = ArtifactId.parse("g:i:1");
        Feature intermediate = FeatureBuilder.assemble(idIntermediate, context, featureA, featureB);
        final ArtifactId id = ArtifactId.parse("g:m:1");
        Feature result = FeatureBuilder.assemble(id, context, featureC, featureD, intermediate);
        ConfigurationApi api = ConfigurationApi.getConfigurationApi(result);
        assertNotNull(api);


        assertEquals(5, api.getFeatureToRegionCache().size());
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureA.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(featureB.getId()));
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureC.getId()));
        assertEquals(Region.INTERNAL, api.getFeatureToRegionCache().get(featureD.getId()));
        assertEquals(Region.GLOBAL, api.getFeatureToRegionCache().get(idIntermediate));
    }
}