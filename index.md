---
title: "Overview"
addon: "ElasticSearch"
repo: "https://github.com/seedstack/elasticsearch-addon"
author: "SeedStack"
description: "Integrates ElasticSearch indexing solution with SeedStack."
min-version: "15.7+"
menu:
    ElasticSearchAddon:
        weight: 10
---

The ElasticSearch add-on allows you to configure, inject and use [ElasticSearch](https://www.elastic.co) clients.

Add it to your application with 
the following Maven dependency:

    <dependency>
        <groupId>org.seedstack.addons</groupId>
        <artifactId>elasticsearch</artifactId>
    </dependency>

Main features:

* Embedded indexes,
* Remote indexes.

{{% callout info %}}
More information about the ElasticSearch Java API [here](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html)
{{% /callout %}}
