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

import org.apache.sling.feature.Artifact;
import org.apache.sling.feature.analyser.task.AnalyserTask;
import org.apache.sling.feature.analyser.task.AnalyserTaskContext;
import org.apache.sling.feature.extension.apiregions.api.artifacts.ArtifactRules;
import org.apache.sling.feature.extension.apiregions.api.artifacts.Mode;
import org.apache.sling.feature.extension.apiregions.api.artifacts.VersionRule;


public class CheckArtifactRules implements AnalyserTask{

    @Override
    public String getId() {
        return "artifact-rules";
    }

    @Override
    public String getName() {
        return "Artifact rules analyser task";
    }

	@Override
	public void execute(final AnalyserTaskContext context) throws Exception {
        final ArtifactRules rules = ArtifactRules.getArtifactRules(context.getFeature());
        if ( rules == null ) {
            context.reportExtensionWarning(ArtifactRules.EXTENSION_NAME, "Artifact rules are not specified, unable to validate feature");
        } else {
            for(final Artifact bundle : context.getFeature().getBundles()) {
                for(final VersionRule rule : rules.getBundleVersionRules()) {
                    if ( rule.getArtifactId() != null && rule.getArtifactId().isSame(bundle.getId())) {
                        if ( ! rule.isAllowed(bundle.getId().getOSGiVersion())) {
                            String msg = rule.getMessage();
                            if ( msg == null ) {
                                msg = "Bundle with version " + bundle.getId().getVersion() + " is not allowed.";
                            }
                            Mode m = rules.getMode();
                            if ( rule.getMode() != null ) {
                                m = rule.getMode();
                            }
                            if ( m == Mode.LENIENT ) {
                                context.reportArtifactWarning(bundle.getId(), msg);
                            } else {
                                context.reportArtifactError(bundle.getId(), msg);
                            }
                        }
                    }
                }
            }
        }
	}
}
