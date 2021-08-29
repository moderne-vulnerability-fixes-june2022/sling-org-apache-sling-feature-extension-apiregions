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
package org.apache.sling.feature.extension.apiregions.analyser;

import static org.mockito.Mockito.when;

import org.apache.sling.feature.Artifact;
import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.analyser.task.AnalyserTaskContext;
import org.apache.sling.feature.extension.apiregions.api.artifacts.ArtifactRules;
import org.apache.sling.feature.extension.apiregions.api.artifacts.Mode;
import org.apache.sling.feature.extension.apiregions.api.artifacts.VersionRule;
import org.junit.Test;
import org.mockito.Mockito;

public class CheckArtifactRulesTest {
    
    private CheckArtifactRules analyser = new CheckArtifactRules();

    private AnalyserTaskContext newContext(final Feature f) {
        final AnalyserTaskContext context = Mockito.mock(AnalyserTaskContext.class);

        when(context.getFeature()).thenReturn(f);
        
        return context;
    }

    @Test public void testValidateFeatureNoRules() throws Exception {
        final Feature f = new Feature(ArtifactId.parse("g:a:1"));

        final AnalyserTaskContext context = newContext(f);
        analyser.execute(context);

        Mockito.verify(context, Mockito.never()).reportError(Mockito.anyString());
        Mockito.verify(context, Mockito.atLeastOnce()).reportExtensionWarning(Mockito.eq(ArtifactRules.EXTENSION_NAME), Mockito.anyString());
    }

    @Test public void testValidateFeature() throws Exception {
        final Feature f = new Feature(ArtifactId.parse("g:a:1"));
        final Artifact bundle = new Artifact(ArtifactId.parse("g:b:1.1"));
        f.getBundles().add(bundle);

        final VersionRule r = new VersionRule();
        r.setArtifactId(bundle.getId());
        r.setMode(Mode.STRICT);
        r.setMessage("foo");

        final ArtifactRules rules = new ArtifactRules();
        rules.getBundleVersionRules().add(r);
        
        ArtifactRules.setArtifactRules(f, rules);
        final AnalyserTaskContext context = newContext(f);
        analyser.execute(context);

        Mockito.verify(context, Mockito.atLeastOnce()).reportArtifactError(Mockito.eq(bundle.getId()), Mockito.eq(r.getMessage()));
    }
}
