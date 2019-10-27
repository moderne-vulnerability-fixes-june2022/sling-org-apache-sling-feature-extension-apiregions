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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.json.JsonArray;

import org.apache.sling.feature.Artifact;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.HandlerContext;
import org.apache.sling.feature.builder.MergeHandler;
import org.apache.sling.feature.extension.apiregions.api.ApiExport;
import org.apache.sling.feature.extension.apiregions.api.ApiRegion;
import org.apache.sling.feature.extension.apiregions.api.ApiRegions;

/**
 * Merge to api region extensions
 */
public class APIRegionMergeHandler implements MergeHandler {

    @Override
    public boolean canMerge(Extension extension) {
        return ApiRegions.EXTENSION_NAME.equals(extension.getName());
    }

    @Override
    public void merge(HandlerContext context, Feature target, Feature source, Extension targetEx, Extension sourceEx) {
        if (!sourceEx.getName().equals(ApiRegions.EXTENSION_NAME))
            return;
        if (targetEx != null && !targetEx.getName().equals(ApiRegions.EXTENSION_NAME))
            return;

        storeBundleOrigins(context, source, target);

        try {
            final ApiRegions srcRegions = ApiRegions.parse((JsonArray) sourceEx.getJSONStructure());

            storeRegionOrigins(context, source, target, srcRegions);

            final ApiRegions targetRegions;
            if (targetEx != null) {
                targetRegions = ApiRegions.parse((JsonArray) targetEx.getJSONStructure());
            } else {
                targetEx = new Extension(sourceEx.getType(), sourceEx.getName(), sourceEx.getState());
                target.getExtensions().add(targetEx);

                targetRegions = new ApiRegions();
            }

            for (final ApiRegion targetRegion : targetRegions.listRegions()) {
                final ApiRegion sourceRegion = srcRegions.getRegionByName(targetRegion.getName());
                if (sourceRegion != null) {
                    srcRegions.remove(sourceRegion);
                    for (final ApiExport srcExp : sourceRegion.listExports()) {
                        if (targetRegion.getExportByName(srcExp.getName()) == null) {
                            targetRegion.add(srcExp);
                        }
                    }
                }
            }

            // If there are any remaining regions in the src extension, process them now
            for (final ApiRegion r : srcRegions.listRegions()) {
                if (!targetRegions.add(r)) {
                    throw new IllegalStateException("Duplicate region " + r.getName());
                }
            }

            targetEx.setJSONStructure(targetRegions.toJSONArray());

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeRegionOrigins(HandlerContext context, Feature source, Feature target, ApiRegions regions) {
        try {
            File f = AbstractHandler.getFeatureDataFile(context, target, "regionOrigins.properties");

            Properties p = new Properties();
            if (f.isFile()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    p.load(fis);
                }
            }

            String fid = source.getId().toMvnId();
            p.put(fid, regions.listRegions().stream().map(region -> region.getName()).collect(Collectors.joining(",")));

            try (FileOutputStream fos = new FileOutputStream(f)) {
                p.store(fos, "Mapping from feature ID to regions that the feature is a member of");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Problem storing region origin information", e);
        }
    }

    private void storeBundleOrigins(HandlerContext context, Feature source, Feature target) {
        try {
            File f = AbstractHandler.getFeatureDataFile(context, target, "bundleOrigins.properties");

            String featureId = source.getId().toMvnId();
            Properties p = new Properties();
            if (f.isFile()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    p.load(fis);
                }
            }

            for (Artifact b : source.getBundles()) {
                String bundleId = b.getId().toMvnId();
                String org = p.getProperty(bundleId);
                String newVal;
                if (org != null) {
                    List<String> l = Arrays.asList(org.split(","));
                    if (!l.contains(featureId))
                        newVal = org + "," + featureId;
                    else
                        newVal = org;
                } else {
                    newVal = featureId;
                }
                p.setProperty(bundleId, newVal);
            }

            try (FileOutputStream fos = new FileOutputStream(f)) {
                p.store(fos, "Mapping from bundle artifact IDs to features that contained the bundle.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Problem storing bundle origin information", e);
        }
    }
}
