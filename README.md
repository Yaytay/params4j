# Parameters For Java

[![License](https://img.shields.io/github/license/yaytay/params4j)]

Parameters for Java is a small library for collating parameters to Java daemons.

Originally written for services running in either Docker Swarm or Kubernetes there are many other places that it can be used.

Params4J can pick up values from:
- Resource files (properties, json or yaml)
- Files on disc (properties, json or yaml)
- Plain files in a hierarchy (aimed at handling Kubernetes secrets)
- Environment variables
- System properties
- Command line arguments
 
It is entirely configurable which of these sources is used.

Files on disc (not resources) can be monitored for any changes.

The results of the gathering of parameters is always a single instance of a POJO of any class that Jackson can deserialize.
I have an intense dislike for the Spring approach of scattering @Values throughout the source code with no collation (or consistency) of the configuration values,
so Params4J takes the approach of forcing all parameters to be defined in a single class structure.
This does not mean that the structure is necessarily flat, nor that it is necessary to pass around all the parameters within your code.
Fields within the parameters object can themselves be POJOs representing a subset of the configuration.

# Getting Started

1. Define the parameters class structure that you want to use.
   This can be as complex as you want, but see [DummyParameters.java](src/test/java/uk/co/spudsoft/params4j/impl/DummyParameters.java) for an example from the unit tests.
2. Decide which sources you want to use for your parameters.
3. Decide whether you want to allow the parameters to be updated.
   It is a good idea to react to parameters being updated, even if your reaction is to just restart.
4. Somewhere near the start of your main process, gather your parameters:
```java
    Params4J<DummyParameters> p4j = new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withGatherer(new PropertiesResourceGatherer<>("/test1.properties"))
            .withGatherer(new DirGatherer<>("/etc/my-service", FileType.Properties, FileType.Yaml))
            .withGatherer(new SecretsGatherer<>("/etc/my-service/conf.d", 100, 100, 4, StandardCharsets.UTF_8))
            .withGatherer(new SystemPropertiesGatherer<>(props, "my-service"))
            .create();

    DummyParameters dp = p4j.gatherParameters();
```