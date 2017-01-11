/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.elasticsearch.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.seedstack.elasticsearch.ElasticSearchConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This plugin manages clients used to access ElasticSearch instances.
 */
public class ElasticSearchPlugin extends AbstractSeedPlugin {
    private static final int DEFAULT_ELASTIC_SEARCH_PORT = 9300;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchPlugin.class);
    private final Map<String, Client> elasticSearchClients = new HashMap<>();

    @Override
    public String name() {
        return "elasticsearch";
    }

    @Override
    public InitState initialize(InitContext initContext) {
        ElasticSearchConfig elasticSearchConfig = getConfiguration(ElasticSearchConfig.class);

        if (!elasticSearchConfig.getClients().isEmpty()) {
            for (Entry<String, ElasticSearchConfig.ClientConfig> clientEntry : elasticSearchConfig.getClients().entrySet()) {
                String clientName = clientEntry.getKey();
                ElasticSearchConfig.ClientConfig clientConfig = clientEntry.getValue();

                List<String> hosts = clientConfig.getHosts();
                LOGGER.info("Creating ElasticSearch client {} for remote instance at {}", clientName, hosts);
                elasticSearchClients.put(clientName, buildRemoteClient(clientName, clientConfig));
            }
        } else {
            LOGGER.info("No ElasticSearch client configured, ElasticSearch support disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new ElasticSearchModule(elasticSearchClients);
    }

    @Override
    public void stop() {
        for (Entry<String, Client> entry : elasticSearchClients.entrySet()) {
            LOGGER.info("Closing ElasticSearch client {}", entry.getKey());
            try {
                entry.getValue().close();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to properly close ElasticSearch client %s", entry.getKey()), e);
            }
        }
    }

    private Client buildRemoteClient(String clientName, ElasticSearchConfig.ClientConfig clientConfig) {
        Settings.Builder settingsBuilder = Settings.builder();
        settingsBuilder.put(clientConfig.getProperties());

        TransportClient transportClient = new PreBuiltTransportClient(settingsBuilder.build(), clientConfig.getPlugins());

        for (String host : clientConfig.getHosts()) {
            String[] hostInfo = host.split(":");
            if (hostInfo.length > 2) {
                throw SeedException.createNew(ElasticSearchErrorCode.INVALID_HOST)
                        .put("clientName", clientName)
                        .put("host", host);
            }
            String address = hostInfo[0].trim();
            int port = DEFAULT_ELASTIC_SEARCH_PORT;
            try {
                if (hostInfo.length > 1) {
                    port = Integer.valueOf(hostInfo[1]);
                }
            } catch (NumberFormatException e) {
                throw SeedException.wrap(e, ElasticSearchErrorCode.INVALID_PORT)
                        .put("clientName", clientName)
                        .put("host", host)
                        .put("port", hostInfo[1]);
            }
            try {
                transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));
            } catch (UnknownHostException e) {
                throw SeedException.wrap(e, ElasticSearchErrorCode.UNKNOWN_HOST)
                        .put("host", address);
            }
        }

        return transportClient;
    }
}
