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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.json.Json;

import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionState;
import org.apache.sling.feature.ExtensionType;
import org.apache.sling.feature.Feature;
import org.junit.Test;

public class ConfigurationApiTest {

    @Test public void testNullFeature() {
        assertNull(ConfigurationApi.getConfigurationApi((Feature)null));
    }

    @Test public void testNullExtension() {
        assertNull(ConfigurationApi.getConfigurationApi((Extension)null));
        final Feature f = new Feature(ArtifactId.parse("g:a:1.0"));
        assertNull(ConfigurationApi.getConfigurationApi(f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongExtensionType() {
        final Feature f = new Feature(ArtifactId.parse("g:a:1.0"));
        final Extension e = new Extension(ExtensionType.TEXT, ConfigurationApi.EXTENSION_NAME, ExtensionState.OPTIONAL);
        f.getExtensions().add(e);
        ConfigurationApi.getConfigurationApi(f);
    }

    @Test public void testClear() {
        final ConfigurationApi entity = new ConfigurationApi();
        entity.getAttributes().put("a", Json.createValue(5));
        entity.getConfigurations().put("pid", new Configuration());
        entity.getFactories().put("factory", new FactoryConfiguration());
        entity.getFrameworkProperties().put("prop", new FrameworkProperty());
        entity.getInternalConfigurations().add("ipid");
        entity.getInternalFactories().add("ifactory");
        entity.getInternalFrameworkProperties().add("iprop");
        entity.clear();
        assertTrue(entity.getAttributes().isEmpty());
        assertTrue(entity.getConfigurations().isEmpty());
        assertTrue(entity.getFactories().isEmpty());
        assertTrue(entity.getFrameworkProperties().isEmpty());
        assertTrue(entity.getInternalConfigurations().isEmpty());
        assertTrue(entity.getInternalFactories().isEmpty());
        assertTrue(entity.getInternalFrameworkProperties().isEmpty());
    }

    @Test public void testFromJSONObject() throws IOException {
        final Extension ext = new Extension(ExtensionType.JSON, "a", ExtensionState.OPTIONAL);
        ext.setJSON("{ \"a\" : 5, \"configurations\" : { \"pid\": {}}, " +
            "\"factories\" : { \"factory\" : {}}," +
            "\"framework-properties\" : { \"prop\" : { \"type\" : \"STRING\"}}," +
            "\"internal-configurations\" : [\"ipid\"],"+
            "\"internal-factories\" : [\"ifactory\"],"+
            "\"internal-framework-properties\" : [\"iprop\"]}");

        final ConfigurationApi entity = new ConfigurationApi();
        entity.fromJSONObject(ext.getJSONStructure().asJsonObject());
        assertEquals(1, entity.getConfigurations().size());
        assertEquals(1, entity.getFactories().size());
        assertEquals(1, entity.getFrameworkProperties().size());
        assertEquals(1, entity.getInternalConfigurations().size());
        assertEquals(1, entity.getInternalFactories().size());
        assertEquals(1, entity.getInternalFrameworkProperties().size());
        assertTrue(entity.getConfigurations().containsKey("pid"));
        assertTrue(entity.getFactories().containsKey("factory"));
        assertTrue(entity.getFrameworkProperties().containsKey("prop"));
        assertTrue(entity.getInternalConfigurations().contains("ipid"));
        assertTrue(entity.getInternalFactories().contains("ifactory"));
        assertTrue(entity.getInternalFrameworkProperties().contains("iprop"));
    }

    @Test public void testToJSONObject() throws IOException {
        final ConfigurationApi entity = new ConfigurationApi();
        entity.getAttributes().put("a", Json.createValue(5));
        entity.getConfigurations().put("pid", new Configuration());
        entity.getFactories().put("factory", new FactoryConfiguration());
        entity.getFrameworkProperties().put("prop", new FrameworkProperty());
        entity.getInternalConfigurations().add("ipid");
        entity.getInternalFactories().add("ifactory");
        entity.getInternalFrameworkProperties().add("iprop");

        final Extension ext = new Extension(ExtensionType.JSON, "a", ExtensionState.OPTIONAL);
        ext.setJSON("{ \"a\" : 5, \"configurations\" : { \"pid\": {}}, " +
            "\"factories\" : { \"factory\" : {}}," +
            "\"framework-properties\" : { \"prop\" : { \"type\" : \"STRING\"}}," +
            "\"internal-configurations\" : [\"ipid\"],"+
            "\"internal-factories\" : [\"ifactory\"],"+
            "\"internal-framework-properties\" : [\"iprop\"]}");

        assertEquals(ext.getJSONStructure().asJsonObject(), entity.toJSONObject());
    }
}
