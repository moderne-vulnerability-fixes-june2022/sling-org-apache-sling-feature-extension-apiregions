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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import javax.json.Json;

import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionState;
import org.apache.sling.feature.ExtensionType;
import org.junit.Test;

public class PropertyTest {

    @Test public void testClear() {
        final Property entity = new Property();
        entity.getAttributes().put("a", Json.createValue(5));
        entity.setDeprecated("d");
        entity.setTitle("t");
        entity.setDescription("x");
        entity.setCardinality(5);
        entity.setExcludes(new String[] {"ex"});
        entity.setIncludes(new String[] {"in"});
        entity.setOptions(Arrays.asList(new Option()));
        entity.setRange(new Range());
        entity.setRegex(".");
        entity.setRequired(true);
        entity.setVariable("var");
        entity.setType(PropertyType.BYTE);
        entity.clear();
        assertTrue(entity.getAttributes().isEmpty());
        assertNull(entity.getDeprecated());
        assertNull(entity.getTitle());
        assertNull(entity.getDescription());
        assertEquals(1, entity.getCardinality());
        assertNull(entity.getExcludes());
        assertNull(entity.getIncludes());
        assertNull(entity.getOptions());
        assertNull(entity.getRange());
        assertNull(entity.getRegex());
        assertNull(entity.getRegexPattern());
        assertNull(entity.getVariable());
        assertFalse(entity.isRequired());
        assertEquals(PropertyType.STRING, entity.getType());
    }

    @Test public void testFromJSONObject() throws IOException {
        final Extension ext = new Extension(ExtensionType.JSON, "a", ExtensionState.OPTIONAL);
        ext.setJSON("{ \"type\" : \"BYTE\", \"cardinality\": 5, \"required\" : true, \"variable\" : \"var\"," +
        "\"range\" : {}, \"includes\" : [\"in\"], \"excludes\" : [\"ex\"] , \"options\": [{}], \"regex\": \".\"}");

        final Property entity = new Property();
        entity.fromJSONObject(ext.getJSONStructure().asJsonObject());

        assertEquals(5, entity.getCardinality());
        assertEquals(PropertyType.BYTE, entity.getType());
        assertTrue(entity.isRequired());
        assertEquals("var", entity.getVariable());
        assertNotNull(entity.getRange());
        assertArrayEquals(new String[] {"ex"}, entity.getExcludes());
        assertArrayEquals(new String[] {"in"}, entity.getIncludes());
        assertEquals(1, entity.getOptions().size());
        assertEquals(".", entity.getRegex());
        assertNotNull(entity.getRegexPattern());

        // test defaults and empty values
        ext.setJSON("{ \"variable\" : \"var\", \"regex\": \".\"}");
        entity.fromJSONObject(ext.getJSONStructure().asJsonObject());

        assertEquals(1, entity.getCardinality());
        assertEquals(PropertyType.STRING, entity.getType());
        assertFalse(entity.isRequired());
        assertEquals("var", entity.getVariable());
        assertNull(entity.getRange());
        assertNull(entity.getExcludes());
        assertNull(entity.getIncludes());
        assertNull(entity.getOptions());
        assertEquals(".", entity.getRegex());
        assertNotNull(entity.getRegexPattern());
   }

    @Test public void testToJSONObject() throws IOException {
        final Property entity = new Property();
        entity.setCardinality(5);
        entity.setExcludes(new String[] {"ex"});
        entity.setIncludes(new String[] {"in"});
        entity.setOptions(Arrays.asList(new Option()));
        entity.setRange(new Range());
        entity.setRegex(".");
        entity.setRequired(true);
        entity.setVariable("var");
        entity.setType(PropertyType.BYTE);

        final Extension ext = new Extension(ExtensionType.JSON, "a", ExtensionState.OPTIONAL);
        ext.setJSON("{ \"type\" : \"BYTE\", \"cardinality\": 5, \"required\" : true, \"variable\" : \"var\"," +
            "\"range\" : {}, \"includes\" : [\"in\"], \"excludes\" : [\"ex\"] , \"options\": [{}], \"regex\": \".\"}");

        assertEquals(ext.getJSONStructure().asJsonObject(), entity.toJSONObject());

        // test defaults and empty values
        entity.setCardinality(1);
        entity.setType(null);
        entity.setRequired(false);
        entity.setRange(null);
        entity.setOptions(null);
        entity.setExcludes(null);
        entity.setIncludes(null);

        ext.setJSON("{ \"type\" : \"STRING\", \"variable\" : \"var\", \"regex\": \".\"}");

        assertEquals(ext.getJSONStructure().asJsonObject(), entity.toJSONObject());
    }
}