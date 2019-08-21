[<img src="https://sling.apache.org/res/logos/sling.png"/>](https://sling.apache.org)

 [![Build Status](https://builds.apache.org/buildStatus/icon?job=Sling/sling-org-apache-sling-feature-extension-apiregions/master)](https://builds.apache.org/job/Sling/job/sling-org-apache-sling-feature-extension-apiregions/job/master) [![Test Status](https://img.shields.io/jenkins/t/https/builds.apache.org/job/Sling/job/sling-org-apache-sling-feature-extension-apiregions/job/master.svg)](https://builds.apache.org/job/Sling/job/sling-org-apache-sling-feature-extension-apiregions/job/master/test_results_analyzer/) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![feature](https://sling.apache.org/badges/group-feature.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/groups/feature.md)

# Apache Sling API Regions extension

This component contains extensions relating to the API Regions component. For more information about API Regions see
https://github.com/apache/sling-org-apache-sling-feature/blob/master/apicontroller.md

The following extensions are registered via the ServiceLoader mechanism:

## `org.apache.sling.feature.builder.MergeHandler`
Merge handlers are called when features are merged during the aggregation process.

`APIRegionMergeHandler` - This handler knows how to merge API Regions extensions and adds the `org-feature` key to the `api-regions` sections to record what feature this section originally belonged.


## `org.apache.sling.feature.builder.PostProcessHandler`
PostProcessHandlers are called when a feature contains an `api-regions` section.

`BundleMappingHandler` - This handler creates a mapping file `idbsnver.properties` that maps the Artifact ID to a bundle symbolic name and version. A tilde `~` is used in the value of the map to separate BSN and version. 

`BundleArtifactFeatureHandler` - This handler creates 3 mapping files:
* `bundles.properties`: maps bundles to the original feature they were in. A bundle could be from more then one feature.
* `features.properties`: maps features to regions. A feature can be in more than one region.
* `regions.properties`: maps regions to packages. A region can expose more than one package.

The location of the files created by the `BundleArtifactFeatureHandler` handler is stored in a system property with name `sling.feature.apiregions.resource.` + filename. So to obtain the file name of the `bundles.properties` file, make the following call:

    System.getProperty("sling.feature.apiregions.resource.bundles.properties")

These properties are read by the https://github.com/apache/sling-org-apache-sling-feature-apiregions component for runtime enforcement of the API Regions.
