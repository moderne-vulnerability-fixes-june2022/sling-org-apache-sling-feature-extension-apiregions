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

import org.apache.sling.feature.Artifact;
import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionType;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.ArtifactProvider;
import org.apache.sling.feature.builder.HandlerContext;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BundleMappingHandlerTest {
    @Test
    public void testHandler() throws IOException {
        ArtifactProvider ap = new ArtifactProvider() {
            @Override
            public File provide(ArtifactId id) {
                switch(id.toMvnId()) {
                case "g:b1:1":
                    return getResourceFile("b1/b1.jar");
                case "g:b2:1.2.3":
                    return getResourceFile("b2/b2.jar");
                case "g:b3:0":
                    return getResourceFile("b3/b3.jar");
                default: return null;
                }
            }
        };

        BundleMappingHandler bmh = new BundleMappingHandler();

        Extension ex = new Extension(ExtensionType.JSON, "api-regions", false);
        Feature f = new Feature(ArtifactId.fromMvnId("foo:bar:123"));
        Artifact b1 = new Artifact(ArtifactId.fromMvnId("g:b1:1"));
        f.getBundles().add(b1);
        Artifact b2 = new Artifact(ArtifactId.fromMvnId("g:b2:1.2.3"));
        f.getBundles().add(b2);
        Artifact b3 = new Artifact(ArtifactId.fromMvnId("g:b3:0"));
        f.getBundles().add(b3);
        bmh.postProcess(new TestHandlerContext(ap), f, ex);

        String p = System.getProperty("apiregions.idbsnver.properties");
        Properties actual = new Properties();
        actual.load(new FileReader(p));

        Properties expected = new Properties();
        expected.put("g:b1:1", "b1~1.0.0");
        expected.put("g:b2:1.2.3", "b2~1.2.3");
        assertEquals(expected, actual);
    }

    @Test
    public void testSpecificDirectory() throws Exception {
        Path tempDir = Files.createTempDirectory(getClass().getSimpleName());

        try {
            ArtifactProvider ap = new ArtifactProvider() {
                @Override
                public File provide(ArtifactId id) {
                    switch(id.toMvnId()) {
                    case "g:b1:1":
                        return getResourceFile("b1/b1.jar");
                    default: return null;
                    }
                }
            };

            Extension ex = new Extension(ExtensionType.JSON, "api-regions", false);
            Feature f = new Feature(ArtifactId.fromMvnId("foo:bar:123"));
            Artifact b1 = new Artifact(ArtifactId.fromMvnId("g:b1:1"));
            f.getBundles().add(b1);

            BundleMappingHandler bmh = new BundleMappingHandler();
            final Map<String, String> kvm = new HashMap<>();
            kvm.put("fileStorage", tempDir.toString());
            bmh.postProcess(new TestHandlerContext(ap,
                    kvm), f, ex);

            File expectedFile = new File(tempDir.toFile(), "foo_bar_123/idbsnver.properties");
            assertTrue(expectedFile.exists());
            Properties p = new Properties();
            p.load(new FileInputStream(expectedFile));

            Properties ep = new Properties();
            ep.put("g:b1:1", "b1~1.0.0");
            assertEquals(ep, p);
        } finally {
            for (File f : tempDir.toFile().listFiles()) {
                f.delete();
            }
            tempDir.toFile().delete();
        }
    }

    @Test
    public void testUnrelatedExtension() {
        BundleMappingHandler bmh = new BundleMappingHandler();
        Extension ex = new Extension(ExtensionType.JSON, "foobar", false);
        bmh.postProcess(null, null, ex);
        // Should not do anything and definitely not throw an exception
    }

    private File getResourceFile(String filename) {
        return new File(getClass().getClassLoader().getResource(filename).getFile());
    }

    private class TestHandlerContext implements HandlerContext {
        private final ArtifactProvider artifactProvider;
        private final Map<String, String> config;

        private TestHandlerContext(ArtifactProvider ap, Map<String, String> cfg) {
            artifactProvider = ap;
            config = cfg;
        }

        public TestHandlerContext(ArtifactProvider ap) {
            this(ap, new HashMap<>());
        }

        @Override
        public ArtifactProvider getArtifactProvider() {
            return artifactProvider;
        }

        @Override
        public Map<String, String> getConfiguration() {
            return config;
        }
    }
}
