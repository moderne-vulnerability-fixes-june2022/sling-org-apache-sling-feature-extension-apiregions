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
package org.apache.sling.feature.extension.apiregions.api.artifacts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.json.Json;

import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionState;
import org.apache.sling.feature.ExtensionType;
import org.apache.sling.feature.Feature;
import org.junit.Test;

public class ArtifactRulesTest {

    @Test public void testNullFeature() {
        assertNull(ArtifactRules.getArtifactRules((Feature)null));
    }

    @Test public void testNullExtension() {
        assertNull(ArtifactRules.getArtifactRules((Extension)null));
        final Feature f = new Feature(ArtifactId.parse("g:a:1.0"));
        assertNull(ArtifactRules.getArtifactRules(f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongExtensionType() {
        final Feature f = new Feature(ArtifactId.parse("g:a:1.0"));
        final Extension e = new Extension(ExtensionType.TEXT, ArtifactRules.EXTENSION_NAME, ExtensionState.OPTIONAL);
        f.getExtensions().add(e);
        ArtifactRules.getArtifactRules(f);
    }

    @Test public void testSetArtifactRules() {
        final ArtifactRules rules = new ArtifactRules();
        final Feature f = new Feature(ArtifactId.parse("g:a:1"));

        assertNull(f.getExtensions().getByName(ArtifactRules.EXTENSION_NAME));

        ArtifactRules.setArtifactRules(f, rules);
        assertNotNull(f.getExtensions().getByName(ArtifactRules.EXTENSION_NAME));
        assertNotNull(ArtifactRules.getArtifactRules(f));

        ArtifactRules.setArtifactRules(f, null);
        assertNull(f.getExtensions().getByName(ArtifactRules.EXTENSION_NAME));
    }

    @Test public void testClear() {
        final ArtifactRules entity = new ArtifactRules();
        entity.getAttributes().put("a", Json.createValue(5));
        entity.setMode(Mode.LENIENT);
        entity.getBundleVersionRules().add(new VersionRule());
        entity.clear();
        assertTrue(entity.getAttributes().isEmpty());
        assertTrue(entity.getBundleVersionRules().isEmpty());
        assertEquals(Mode.STRICT, entity.getMode());
    }
}
