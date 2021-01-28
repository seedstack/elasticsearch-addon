# SeedStack ElasticSearch add-on

[![Build status](https://travis-ci.org/seedstack/elasticsearch-addon.svg?branch=master)](https://travis-ci.org/seedstack/elasticsearch-addon) [![Coverage Status](https://coveralls.io/repos/seedstack/elasticsearch-addon/badge.svg?branch=master)](https://coveralls.io/r/seedstack/elasticsearch-addon?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.seedstack.addons.elasticsearch/elasticsearch/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.seedstack.addons.elasticsearch/elasticsearch)

ElasticSearch official persistence integration for SeedStack.

This addon provides a transport client compatible with elasticsearch from v2 to v7.

It has been tested against following elasticsearch servers:

- 2.4.6
- 5.6.2
- 6.8.10
- 7.9.1

Profiles have added in order to help build the addon with various elasticsearch jar versions. 
By default, Profile v2 is used in travis build configuration. 

# Copyright and license

This source code is copyrighted by [The SeedStack Authors](https://github.com/seedstack/seedstack/blob/master/AUTHORS) and
released under the terms of the [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/). 
