---
title: "Basics"
name: "ElasticSearch"
repo: "https://github.com/seedstack/elasticsearch-addon"
date: 2015-11-17
author: Redouane LOULOU
description: "Integrates ElasticSearch indexing solution with SeedStack."
backend: true
weight: -1
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

{{< dependency g="org.seedstack.addons.elasticsearch" a="elasticsearch" >}}

Main features:

* Embedded indexes,
* Remote indexes.

{{% callout info %}}
More information about the ElasticSearch Java API [here](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html)
{{% /callout %}}

# Configuration

To access an ElasticSearch index, you need to declare a client in configuration. Multiple clients can be configured. They
must be listed in the following property:

```ini
org.seedstack.elasticsearch.clients = client1, client2, ...
```

## Remote instance

To access a remote ElasticSearch index, you need to specify the host(s) of one or more node(s) of the ElasticSearch
cluster:

```ini
[org.seedstack.elasticsearch.client.client1]
hosts = host1:port1, host2:port2, ...
```

You can omit the port in which case will be set to the ElasticSearch default (9300).

## Embedded instance

If you don't specify the `hosts` property, a local ElasticSearch node will be created and stored in the `persistence-elasticsearch/{client-name}`
subdirectory of the Seed local storage location, where `{client-name}` is the name of the ElasticSearch client.

## Other options

You can specify any configuration property of the ElasticSearch client with the following syntax:

```ini
[org.seedstack.elasticsearch.client.client1]
property.name.of.elasticsearch.property = value
```

# Usage

To use a configured ElasticSearch client, simply inject it where needed:

```java
@Inject
@Named("client1")
Client client1;
```

# Example

Configuration for an embedded ElasticSearch instance:

```ini
org.seedstack.elasticsearch.clients = test

[org.seedstack.elasticsearch.client.test]
property.cluster.name = test-cluster-1
```

To inject this configured client, use the following syntax:

```ini
@Inject
@Named("test")
Client testClient;
```

