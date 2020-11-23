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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Configuration;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.FeatureProvider;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationApi;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FactoryConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FrameworkPropertyDescription;
import org.apache.sling.feature.extension.apiregions.api.config.Operation;
import org.apache.sling.feature.extension.apiregions.api.config.PropertyDescription;
import org.apache.sling.feature.extension.apiregions.api.config.PropertyType;
import org.apache.sling.feature.extension.apiregions.api.config.Region;
import org.junit.Before;
import org.junit.Test;

public class FeatureValidatorTest {
    
    private static final String PID = "org.apache.sling";

    private static final String FACTORY_PID = "org.apache.sling.factory";

    private final FeatureValidator validator = new FeatureValidator();

    @Before public void setup() {
        this.validator.setFeatureProvider(null);
    }

    private Feature createFeature(final String id) {
        final Feature f= new Feature(ArtifactId.parse(id));
        final Configuration c = new Configuration(PID);
        c.getProperties().put("prop", "a");
        f.getConfigurations().add(c);

        final Configuration fc = new Configuration(FACTORY_PID.concat("~print"));
        fc.getProperties().put("key", "value");
        f.getConfigurations().add(fc);

        f.getFrameworkProperties().put("prop", "1");

        return f;
    }

    private ConfigurationApi createApi() {
        final ConfigurationApi api = new ConfigurationApi();

        final ConfigurationDescription cd = new ConfigurationDescription();
        cd.getPropertyDescriptions().put("prop", new PropertyDescription());

        api.getConfigurationDescriptions().put(PID, cd);

        final FactoryConfigurationDescription fd = new FactoryConfigurationDescription();
        fd.getPropertyDescriptions().put("key", new PropertyDescription());

        api.getFactoryConfigurationDescriptions().put(FACTORY_PID, fd);

        final FrameworkPropertyDescription fpd = new FrameworkPropertyDescription();
        fpd.setType(PropertyType.INTEGER);
        api.getFrameworkPropertyDescriptions().put("prop", fpd);

        return api;
    }

    @Test public void testGetRegionInfoConfigurationNoOrigin() {
        final Feature f1 = createFeature("g:a:1");
        final Configuration cfg = f1.getConfigurations().getConfiguration(PID);

        // no api set
        FeatureValidator.RegionInfo info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // empty region in api
        final ConfigurationApi api = createApi();
        ConfigurationApi.setConfigurationApi(f1, api);
        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // global region in api
        api.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // internal region in api
        api.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.INTERNAL, info.region);
        assertFalse(info.isUpdate);
    }
     
    @Test public void testGetRegionInfoConfigurationSingleOrigin() {
        final Feature f1 = createFeature("g:a:1");
        final Configuration cfg = f1.getConfigurations().getConfiguration(PID);

        final Feature f2 = createFeature("g:b:1");
        cfg.setFeatureOrigins(Collections.singletonList(f2.getId()));

        // set feature provider to always provide f2
        this.validator.setFeatureProvider(id -> f2);
        // no api in origin
        FeatureValidator.RegionInfo info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // no region in api
        final ConfigurationApi api2 = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(f2, api2);
        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // global in api
        api2.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(f2, api2);
        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // internal in api
        api2.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f2, api2);
        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.INTERNAL, info.region);
        assertFalse(info.isUpdate);

        // unknown id
        this.validator.setFeatureProvider(id -> null);
        cfg.setFeatureOrigins(Collections.singletonList(ArtifactId.parse("g:xy:1")));
        info = validator.getRegionInfo(f1, cfg);
        assertNull(info);
    }

    @Test public void testGetRegionInfoConfigurationMultipleOrigins() {
        final Feature f1 = createFeature("g:a:1");
        final Configuration cfg = f1.getConfigurations().getConfiguration(PID);

        final Feature f2 = createFeature("g:b:1");
        final Feature f3 = createFeature("g:c:1");
        cfg.setFeatureOrigins(Arrays.asList(f2.getId(), f3.getId()));

        final FeatureProvider provider = new FeatureProvider() {

			@Override
			public Feature provide(final ArtifactId id) {
                if ( f1.getId().equals(id) ) {
                    return f1;
                } else if ( f2.getId().equals(id)) {
                    return f2;
                } else if ( f3.getId().equals(id)) {
                    return f3;
                }
				return null;
			}
            
        };

        this.validator.setFeatureProvider(provider);

        // no api in origins
        FeatureValidator.RegionInfo info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);

        // global-internal
        final ConfigurationApi api2 = new ConfigurationApi();
        final ConfigurationApi api3 = new ConfigurationApi();
        api2.setRegion(Region.GLOBAL);
        api3.setRegion(Region.INTERNAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);

        // global-global
        api2.setRegion(Region.GLOBAL);
        api3.setRegion(Region.GLOBAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);

        // internal-internal
        api2.setRegion(Region.INTERNAL);
        api3.setRegion(Region.INTERNAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.INTERNAL, info.region);
        assertTrue(info.isUpdate);

        // internal-global
        api2.setRegion(Region.INTERNAL);
        api3.setRegion(Region.GLOBAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, cfg);
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);
    }

    @Test public void testGetRegionInfoFrameworkPropertyNoOrigin() {
        final Feature f1 = createFeature("g:a:1");

        // no api set
        FeatureValidator.RegionInfo info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // empty region in api
        final ConfigurationApi api = createApi();
        ConfigurationApi.setConfigurationApi(f1, api);
        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // global region in api
        api.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // internal region in api
        api.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.INTERNAL, info.region);
        assertFalse(info.isUpdate);
    }
     
    @Test public void testGetRegionInfoFrameworkPropertySingleOrigin() {
        final Feature f1 = createFeature("g:a:1");

        final Feature f2 = createFeature("g:b:1");
        f1.setFeatureOrigins(f1.getFrameworkPropertyMetadata("prop"), Collections.singletonList(f2.getId()));

        // set feature provider to always provide f2
        this.validator.setFeatureProvider(id -> f2);
        // no api in origin
        FeatureValidator.RegionInfo info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // no region in api
        final ConfigurationApi api2 = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(f2, api2);
        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // global in api
        api2.setRegion(Region.GLOBAL);
        ConfigurationApi.setConfigurationApi(f2, api2);
        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertFalse(info.isUpdate);

        // internal in api
        api2.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f2, api2);
        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.INTERNAL, info.region);
        assertFalse(info.isUpdate);

        // unknown id
        this.validator.setFeatureProvider(id -> null);
        f1.setFeatureOrigins(f1.getFrameworkPropertyMetadata("prop"), Collections.singletonList(ArtifactId.parse("g:xy:1")));
        info = validator.getRegionInfo(f1, "prop");
        assertNull(info);
    }

    @Test public void testGetRegionInfoFrameworkPropertyMultipleOrigins() {
        final Feature f1 = createFeature("g:a:1");

        final Feature f2 = createFeature("g:b:1");
        final Feature f3 = createFeature("g:c:1");
        f1.setFeatureOrigins(f1.getFrameworkPropertyMetadata("prop"), Arrays.asList(f2.getId(), f3.getId()));

        final FeatureProvider provider = new FeatureProvider() {

			@Override
			public Feature provide(final ArtifactId id) {
                if ( f1.getId().equals(id) ) {
                    return f1;
                } else if ( f2.getId().equals(id)) {
                    return f2;
                } else if ( f3.getId().equals(id)) {
                    return f3;
                }
				return null;
			}
            
        };

        this.validator.setFeatureProvider(provider);

        // no api in origins
        FeatureValidator.RegionInfo info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);

        // global-internal
        final ConfigurationApi api2 = new ConfigurationApi();
        final ConfigurationApi api3 = new ConfigurationApi();
        api2.setRegion(Region.GLOBAL);
        api3.setRegion(Region.INTERNAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);

        // global-global
        api2.setRegion(Region.GLOBAL);
        api3.setRegion(Region.GLOBAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);

        // internal-internal
        api2.setRegion(Region.INTERNAL);
        api3.setRegion(Region.INTERNAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.INTERNAL, info.region);
        assertTrue(info.isUpdate);

        // internal-global
        api2.setRegion(Region.INTERNAL);
        api3.setRegion(Region.GLOBAL);        
        ConfigurationApi.setConfigurationApi(f2, api2);
        ConfigurationApi.setConfigurationApi(f3, api3);

        info = validator.getRegionInfo(f1, "prop");
        assertEquals(Region.GLOBAL, info.region);
        assertTrue(info.isUpdate);
    }

    @Test public void testSingleConfigurationValidation() {
        final Feature f1 = createFeature("g:a:1");
        final ConfigurationApi api = createApi();
        ConfigurationApi.setConfigurationApi(f1, api);

        FeatureValidationResult result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // add property
        f1.getConfigurations().getConfiguration(PID).getProperties().put("b", "x");
        result = validator.validate(f1, api);
        assertFalse(result.isValid());
    }

    @Test public void testInternalConfiguration() {
        final Feature f1 = createFeature("g:a:1");
        final ConfigurationApi api = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(f1, api);

        // global region
        FeatureValidationResult result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // mark configurations as internal
        api.getInternalConfigurations().add(PID);
        api.getInternalFactoryConfigurations().add(FACTORY_PID);
        ConfigurationApi.setConfigurationApi(f1, api);

        // global region
        result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getConfigurationResults().get(PID).isValid());
        assertFalse(result.getConfigurationResults().get(FACTORY_PID.concat("~print")).isValid());

        // internal region
        api.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());
    }

    @Test public void testInternalFactoryNames() {
        final Feature f1 = createFeature("g:a:1");

        final Configuration fa = new Configuration(FACTORY_PID.concat("~a"));
        fa.getProperties().put("key", "value");
        f1.getConfigurations().add(fa);

        final Configuration fb = new Configuration(FACTORY_PID.concat("~b"));
        fb.getProperties().put("key", "value");
        f1.getConfigurations().add(fb);

        final ConfigurationApi api = createApi();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getInternalNames().add("a");
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getInternalNames().add("b");
        ConfigurationApi.setConfigurationApi(f1, api);

        FeatureValidationResult result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getConfigurationResults().get(FACTORY_PID.concat("~a")).isValid());
        assertFalse(result.getConfigurationResults().get(FACTORY_PID.concat("~b")).isValid());
        assertTrue(result.getConfigurationResults().get(FACTORY_PID.concat("~print")).isValid());

       // internal region
       api.setRegion(Region.INTERNAL);
       ConfigurationApi.setConfigurationApi(f1, api);
       result = validator.validate(f1, api);
       assertTrue(result.isValid());
    }

    @Test public void testFactoryConfigurationOperationsWithCreate() {
        final Feature f1 = createFeature("g:a:1");
        final ConfigurationApi api = createApi();

        // no operation -> fail
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        ConfigurationApi.setConfigurationApi(f1, api);
        FeatureValidationResult result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getConfigurationResults().get(FACTORY_PID.concat("~print")).isValid());

        // only update -> fail
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.UPDATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getConfigurationResults().get(FACTORY_PID.concat("~print")).isValid());

        // only create -> success
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.CREATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // update, create -> success
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.CREATE);
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.UPDATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // internal region -> always success
        api.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.UPDATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.CREATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());
    }

    @Test public void testFactoryConfigurationOperationsWithUpdate() {
        final Feature f1 = createFeature("g:a:1");
        final ConfigurationApi api = createApi();

        final Configuration cfg = f1.getConfigurations().getConfiguration(FACTORY_PID.concat("~print"));

        final Feature f2 = createFeature("g:b:1");
        final Feature f3 = createFeature("g:c:1");
        cfg.setFeatureOrigins(Arrays.asList(f2.getId(), f3.getId()));

        final FeatureProvider provider = new FeatureProvider() {

			@Override
			public Feature provide(final ArtifactId id) {
                if ( f1.getId().equals(id) ) {
                    return f1;
                } else if ( f2.getId().equals(id)) {
                    return f2;
                } else if ( f3.getId().equals(id)) {
                    return f3;
                }
				return null;
			}
            
        };

        this.validator.setFeatureProvider(provider);

        // no operation -> fail
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        ConfigurationApi.setConfigurationApi(f1, api);
        FeatureValidationResult result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getConfigurationResults().get(FACTORY_PID.concat("~print")).isValid());

        // only update -> success
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.UPDATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // only create -> fail
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.CREATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getConfigurationResults().get(FACTORY_PID.concat("~print")).isValid());

        // update, create -> success
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.CREATE);
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.UPDATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // internal region -> always success
        api.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        ConfigurationApi.setConfigurationApi(f2, api);
        ConfigurationApi.setConfigurationApi(f3, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.UPDATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().add(Operation.CREATE);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        api.getFactoryConfigurationDescriptions().get(FACTORY_PID).getOperations().clear();
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());
    }

    @Test public void testInternalFrameworkProperty() {
        final Feature f1 = createFeature("g:a:1");
        final ConfigurationApi api = new ConfigurationApi();
        ConfigurationApi.setConfigurationApi(f1, api);

        // global region
        FeatureValidationResult result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // mark framework property as internal
        api.getInternalFrameworkProperties().add("prop");
        ConfigurationApi.setConfigurationApi(f1, api);

        // global region
        result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getFrameworkPropertyResults().get("prop").isValid());

        // internal region
        api.setRegion(Region.INTERNAL);
        ConfigurationApi.setConfigurationApi(f1, api);
        result = validator.validate(f1, api);
        assertTrue(result.isValid());
    }

    @Test public void testFrameworkProperty() {
        final Feature f1 = createFeature("g:a:1");
        final ConfigurationApi api = createApi();
        ConfigurationApi.setConfigurationApi(f1, api);

        // value is valid
        FeatureValidationResult result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // no value -> valid
        f1.getFrameworkProperties().remove("prop");
        result = validator.validate(f1, api);
        assertTrue(result.isValid());

        // invalid value
        f1.getFrameworkProperties().put("prop", "foo");
        result = validator.validate(f1, api);
        assertFalse(result.isValid());
        assertFalse(result.getFrameworkPropertyResults().get("prop").isValid());
    }
}
