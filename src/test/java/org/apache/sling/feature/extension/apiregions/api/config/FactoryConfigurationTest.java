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

import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionState;
import org.apache.sling.feature.ExtensionType;
import org.junit.Test;

public class FactoryConfigurationTest {

    @Test public void testClear() {
        final FactoryConfiguration entity = new FactoryConfiguration();
        entity.getAttributes().put("a", Json.createValue(5));
        entity.setDeprecated("d");
        entity.setTitle("t");
        entity.setDescription("x");
        entity.getProperties().put("a", new Property());
        entity.getOperations().add(Operation.CREATE);
        entity.getInternalNames().add("internal");
        entity.clear();
        assertTrue(entity.getAttributes().isEmpty());
        assertNull(entity.getDeprecated());
        assertNull(entity.getTitle());
        assertNull(entity.getDescription());
        assertTrue(entity.getProperties().isEmpty());
        assertTrue(entity.getOperations().isEmpty());
        assertTrue(entity.getInternalNames().isEmpty());
    }

    @Test public void testFromJSONObject() throws IOException {
        final Extension ext = new Extension(ExtensionType.JSON, "a", ExtensionState.OPTIONAL);
        ext.setJSON("{ \"internal-names\" : [ \"a\", \"b\"], \"operations\" : [\"create\"]}");

        final FactoryConfiguration entity = new FactoryConfiguration();
        entity.fromJSONObject(ext.getJSONStructure().asJsonObject());
        assertEquals(2, entity.getInternalNames().size());
        assertTrue(entity.getInternalNames().contains("a"));
        assertTrue(entity.getInternalNames().contains("b"));
        assertEquals(1, entity.getOperations().size());
        assertEquals(Operation.CREATE, entity.getOperations().iterator().next());
    }

    @Test public void testToJSONObject() throws IOException {
        final FactoryConfiguration entity = new FactoryConfiguration();
        entity.getInternalNames().add("a");
        entity.getInternalNames().add("b");
        entity.getOperations().add(Operation.UPDATE);

        final Extension ext = new Extension(ExtensionType.JSON, "a", ExtensionState.OPTIONAL);
        ext.setJSON("{ \"internal-names\" : [ \"a\", \"b\"], \"operations\" : [\"UPDATE\"]}");

        assertEquals(ext.getJSONStructure().asJsonObject(), entity.toJSONObject());
    }
}