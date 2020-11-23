# API Regions for the Feature Model

If you're assembling a platform (in contrast to a final application) out of several features and provide this platform for customers to build their application on top of, additional control of the API provided by the platform is needed. The bundles within the features provide all kinds of APIs but you might not want to expose all of these as extension points. You would rather want to use some of them internally within either a single feature or share within your platform features.

## Visibility of Java API

A feature exports some API, however there are two different types of clients of the API:

* Bundles shipped as part of the platform
* Application bundles using the platform

We can generalize this by saying that API is either globally visible (to every client) or only visible to features within the same context. Usually this is referred to as a "region": The platform spawns its own region and a customer application has its own region, too. In theory there could be several customer applications running in the same framework on top of the platform, and each application has its own region.

Without any further information, API is globally visible by default. However, for platform features we want the opposite as we want to ensure that newly added API is not visible to all bundles by default.

A feature can have an additional extension of type JSON named `api-region`. The following example exposes some packages to the global region and an additional package to the platform region. Exports declared earlier in the api-regions array also apply to later elements in the array, so the `platform` region also contains all exports declared for the `global` region.

Note that the `global` region is a predefined region that exports the listed packages to everyone. Other region names can be chosen freely. Packages listed in these other regions are only exposed to bundles in features that are in the same region.

``` json
    "api-regions:JSON|optional" : [
        {
            "name": "global",
            "exports": [
                "# Export Sling's resource API in the global region",
                "org.apache.sling.resource.api",
                "org.apache.sling.resource.api.adapter",
                "org.apache.sling.resource.api.auth",
                "org.apache.sling.resource.api.request",
                "org.apache.sling.resource.api.resource"
            ]
        },{
            "name": "platform",
            "exports": [
                "# Export the scheduler API in the platform region.",
                "# All exports in earlier regions defined here also apply.",
                "org.apache.sling.commons.scheduler"
            ]
        }
    ]
```

Of course the above mentioned packages need to be exported by some bundle within the feature. By exporting packages to a given region, a feature automatically also sees all packages available to that region (or regions).

A feature can also just consume packages from a region, without having to export any packages to it. This can be done by exporting an empty list of packages. For example:

``` json
    "api-regions:JSON|optional" : [
        {
            "name": "platform",
            "exports": []
        }
    ]
```

If the api-regions extension is missing or the api-regions information is missing, it is assumed that all packages are exported to the "global" region and all packages in the global region are visible to the feature.

If a feature exports no packages and only wants to have visibility of packages from the global region, this can be specified as follows:

``` json
    "api-regions:JSON|optional" : [
        {
            "name": "global",
            "exports": []
        }
    ]
```

## Deprecation of Java API

The usual process for deprecating Java API is to mark it with a corresponding annotation and javadoc tag. However, in cases where changing the source code of an API is not possible to add such a marker, the deprecation can be specified through an API region. This information can be used by tooling to mark and report usage of such API.

``` json
     "api-regions:JSON|false":[
        {
            "name":"global",
            "exports":[
                # Package exported without deprecation
                "org.apache.sling.api",
                # Package exported - full package deprecated
                {
                    "name" : "org.apache.sling.api.file",
                    "deprecated" : "Deprecation message"
                },
                # Package exported - single class and single member deprecated
                {
                    "name" : "org.apache.sling.api.io",
                    "deprecated" : {
                        "members" : {
                            "FileCache" : "Deprecation message",
                            "MemoryCache#getFile()" : "Deprecation message"
                        }
                    }
                },
                # Instead of just the message, additional information about when the
                # deprecation happened can be provided (this works in all of the
                # above places)
                {
                    "name":"org.apache.sling.incubator.api",
                    "deprecated":{
                        "msg":"This is deprecated",
                        "since":"Since Sling left the incubator"
                    }
                }
            ]
        }
     ]
```

## OSGi Configurations

Apart from defining the Java API surface, for some applications it is benefitial to describe the OSGi configuration surface, too. For example, a framework might not allow an application to set some configurations or update existing configurations.

While the [OSGi metatype specification](https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.metatype.html) allows to specify some type information for OSGi configurations, it does not allow to specify validation rules or differentiate between a private and a public configuration or configuration property.

For each configuration and factory configuration, the allowed list of properties can be specified together with a type and additional validation rules (like a range or a regualr expression). Properties not mentioned for a configuration are considered internal. In addition, configuration PIDs and factory configuration PIDs can be specified as internal as well.

A similar mechanism exists for framework properties.

``` json
"configuration-api:JSON|optional" : {
  "configurations" : {
    "PID-A" : {
      "title":"Configuration XY",
      "description:":"This configuration...",
      "properties" : {
        "a" : {
           "type" : "string|long|int|short|char|byte|double|float|boolean|password|url|email", // string is default
           "cardinality" : 1, // optional, defaults to 1; if -1 == unlimited array, otherwise must be > 0; value > 1 == max
           "title" : "...",    // optional
           "description" : "...", // optional
           "options" : [ // optional
              {"title" : "value"}, {"another title":"another value"}
            ],
            "range" : {
               "min" : 1000,
               "max" : 5000
             },
            "variable" : "APP_PORT" // optional
        }
      }
    }
  },
  "factory-configurations" : {
    "FACTORY_PID_A" : {
      "title":"Configuration ZY",
      "description":"...",
      "properties":{
         ...,
         "variable" : "APP_LOG_LEVEL_{name}" // the variable name will contain the name of the factory configuration
      },
      "operations":["create", "update"], // add allows customers to create new factory configurations, update allows to change existing ones
      "internal-names" : ["...", "..."] // optional list of names for factory configurations which are internal and can't be changed by customers
    }
  },
  "internal-configurations" : [
    // list of PIDs
  ],
  "internal-factory-configurations" : [
    // list of factory PIDs
  ],
  "framework-properties" : {
    "prop-a" : {
      // title, description
      // same properties as a single configuration property
    }
  },
  "internal-framework-properties" : {
    // list of property names
  }
}
```

### OSGi Configuration Regions

Two regions are supported for OSGi configurations, internal and global. Without any further information, a feature is considered to be part of the global configuration region. If a feature wants to be part of the internal region it can specify this in the extension:

``` json
"configuration-api:JSON|optional" : {
  "region" : "INTERNAL"
}
```

When two features are aggregated, the resulting feature is only in the internal region if both source features are in the internal region. Otherwise, the resulting aggregate is always in the global region.
