---
title: "ElasticSearch"
repo: "https://github.com/seedstack/elasticsearch-addon"
author: Redouane LOULOU
description: "Integrates ElasticSearch indexing solution with SeedStack."
tags:
    - "persistence"
    - "indexing"
    - "elastic"
    - "search"
    - "data"
zones:
    - Addons
menu:
    ElasticSearchAddon:
        weight: 10
---

The ElasticSearch add-on allows you to configure, inject and use [ElasticSearch](https://www.elastic.co) clients.

# Dependency

{{< dependency g="org.seedstack.addons.elasticsearch" a="elasticsearch" >}}

# Configuration

To access an ElasticSearch index, you need to declare a client in configuration. Multiple clients can be configured:

{{% config p="elasticSearch" %}}
```yaml
elasticSearch:
  # Configured ElasticSearch clients with the client name as key
  clients:
    client1:
      # List of hosts the client will connect to
      hosts: (List<String>)
      
      # Properties used for client configuration
      properties:
        property1: value1
        
      # Set of ElasticSearch plugin classes to enable for this client  
      plugins: Set<Class<? extends Plugin>>
```
{{% /config %}}

# Usage

To use a configured ElasticSearch client, simply inject it where needed:

```java
public class SomeClass {
    @Inject
    @Named("client1")
    private Client client1;
}
```

{{% callout ref %}}
You can find more information about the ElasticSearch Java API [here](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html).
{{% /callout %}}

# Example

Configuration for an ElasticSearch server on the same machine:

```yaml
elasticSearch:
  clients:
    client1: localhost
```

This client is used like this:

```java
public class SomeClass {
    @Inject
    @Named("client1")
    private Client client1;
}
```

