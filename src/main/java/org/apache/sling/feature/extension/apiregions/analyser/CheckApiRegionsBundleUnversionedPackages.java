/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.feature.extension.apiregions.analyser;

import org.apache.sling.feature.analyser.task.AnalyserTask;
import org.apache.sling.feature.analyser.task.AnalyserTaskContext;
import org.apache.sling.feature.extension.apiregions.api.ApiRegions;
import org.apache.sling.feature.scanner.BundleDescriptor;
import org.apache.sling.feature.scanner.PackageInfo;
import org.osgi.framework.Version;

import java.util.*;

public class CheckApiRegionsBundleUnversionedPackages implements AnalyserTask {

    /** Ignore JDK packages */
    private static final List<String> IGNORED_IMPORT_PREFIXES = Arrays.asList("java.", "javax.", "org.w3c.", "org.xml.");

    @Override
    public String getName() {
        return "Bundle Unversioned Packages Check";
    }

    @Override
    public String getId() {
        return ApiRegions.EXTENSION_NAME + "-unversioned-packages";
    }

    public static final class Report {

        public List<PackageInfo> exportWithoutVersion = new ArrayList<>();

        public List<PackageInfo> importWithoutVersion = new ArrayList<>();

    }

    private Report getReport(final Map<BundleDescriptor, Report> reports, final BundleDescriptor info) {
        Report report = reports.get(info);
        if ( report == null ) {
            report = new Report();
            reports.put(info, report);
        }
        return report;
    }

    private void checkForVersionOnExportedPackages(final AnalyserTaskContext ctx, final Map<BundleDescriptor, Report> reports) {
        for(final BundleDescriptor info : ctx.getFeatureDescriptor().getBundleDescriptors()) {
            if ( info.getExportedPackages() != null ) {
                for(final PackageInfo i : info.getExportedPackages()) {
                    if ( i.getPackageVersion().compareTo(Version.emptyVersion) == 0 ) {
                        getReport(reports, info).exportWithoutVersion.add(i);
                    }
                }
            }
        }
    }

    private boolean ignoreImportPackage(final String name) {
        for(final String prefix : IGNORED_IMPORT_PREFIXES) {
            if ( name.startsWith(prefix) ) {
                return true;
            }
        }
        return false;
    }
    
    private void checkForVersionOnImportingPackages(final AnalyserTaskContext ctx, final Map<BundleDescriptor, Report> reports) {
        for(final BundleDescriptor info : ctx.getFeatureDescriptor().getBundleDescriptors()) {
            if ( info.getImportedPackages() != null ) {
                for(final PackageInfo i : info.getImportedPackages()) {
                    if ( i.getVersion() == null && !ignoreImportPackage(i.getName()) ) {
                        getReport(reports, info).importWithoutVersion.add(i);
                    }
                }
            }
        }
    }

    @Override
    public void execute(final AnalyserTaskContext ctx) throws Exception {
        final Map<BundleDescriptor, Report> reports = new HashMap<>();
        checkForVersionOnExportedPackages(ctx, reports);
        checkForVersionOnImportingPackages(ctx, reports);

        for(final Map.Entry<BundleDescriptor, Report> entry : reports.entrySet()) {
            final String key = "Bundle " + entry.getKey().getArtifact().getId().getArtifactId() + ":" + entry.getKey().getArtifact().getId().getVersion();

            if ( !entry.getValue().importWithoutVersion.isEmpty() ) {
                ctx.reportArtifactWarning(entry.getKey().getArtifact().getId(),
                        key + " is importing package(s) " + getPackageInfo(entry.getValue().importWithoutVersion) + " without specifying a version range.");
            }
            if ( !entry.getValue().exportWithoutVersion.isEmpty() ) {
                ctx.reportArtifactWarning(entry.getKey().getArtifact().getId(),
                        key + " is exporting package(s) " + getPackageInfo(entry.getValue().exportWithoutVersion) + " without a version.");
            }
        }
    }

    private String getPackageInfo(final List<PackageInfo> pcks) {
        if ( pcks.size() == 1 ) {
                return pcks.get(0).getName();
        }
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append('[');
        for(final PackageInfo info : pcks) {
            if ( first ) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(info.getName());
        }
        sb.append(']');
        return sb.toString();
    }

}
