/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
import org.elasticsearch.plugins.Plugin;
import org.seedstack.elasticsearch.ElasticSearchConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Collection;
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
        for ( Entry<Object, Object> entry: clientConfig.getProperties().entrySet() ) {
            settingsBuilder.put((String) entry.getKey(), (String) entry.getValue());
        }

        TransportClient transportClient = null;
        if ( Classes.optional("org.elasticsearch.client.transport.TransportClient$Builder").isPresent() )  {
            transportClient = createV2TransportClient(clientName, clientConfig, settingsBuilder.build());
        } else if (Classes.optional("org.elasticsearch.transport.client.PreBuiltTransportClient").isPresent() ) {
            transportClient =  createPreBuiltTransportClient(clientName, clientConfig, settingsBuilder.build());
        } else {
            throw SeedException.createNew(ElasticSearchErrorCode.NO_TRANSPORT_CLIENT_FOUND)
                    .put("clientName", clientName);
        }

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
                    port = Integer.parseInt(hostInfo[1]);
                }
            } catch (NumberFormatException e) {
                throw SeedException.wrap(e, ElasticSearchErrorCode.INVALID_PORT)
                        .put("clientName", clientName)
                        .put("host", host)
                        .put("port", hostInfo[1]);
            }
            addTransportAddress(transportClient, address, port);
        }

        return transportClient;
    }

    private TransportClient createV2TransportClient(String clientName, ElasticSearchConfig.ClientConfig clientConfig, Settings settings) {
        try {
            Object builder = TransportClient.class.getMethod("builder").invoke(null);
            Class<?> transportClientBuilderClass = Class.forName("org.elasticsearch.client.transport.TransportClient$Builder");
            transportClientBuilderClass.getMethod("settings", Settings.class).invoke(builder, settings);
            Method addPluginMethod = transportClientBuilderClass.getMethod("addPlugin", Class.class);
            for (Class<? extends Plugin> pluginClass : clientConfig.getPlugins()) {
                addPluginMethod.invoke(builder, pluginClass);
            }
            return (TransportClient) transportClientBuilderClass.getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw SeedException.wrap(e, ElasticSearchErrorCode.CANNOT_CREATE_CLIENT)
                    .put("clientName", clientName);
        }
    }

    private TransportClient createPreBuiltTransportClient(String clientName, ElasticSearchConfig.ClientConfig clientConfig, Settings settings) {
        try {
            Class<?> preBuiltTransportClientClass = Class.forName("org.elasticsearch.transport.client.PreBuiltTransportClient");
            Constructor<?> constructor = preBuiltTransportClientClass.getConstructor(Settings.class, Collection.class);
            return (TransportClient) constructor.newInstance(settings, clientConfig.getPlugins());
        } catch (Exception e) {
            throw SeedException.wrap(e, ElasticSearchErrorCode.CANNOT_CREATE_CLIENT)
                    .put("clientName", clientName);
        }
    }

    private void addTransportAddress(TransportClient transportClient, String address, int port) throws SeedException {
        try {
            Class<?> transportAddressClass = Classes.optional("org.elasticsearch.common.transport.InetSocketTransportAddress")
            .orElse((Class<Object>) Class.forName("org.elasticsearch.common.transport.TransportAddress"));

            Constructor<?> transportAddressClassConstructor = transportAddressClass.getConstructor(InetAddress.class, Integer.TYPE);
            Method addTransportAddressMethod = transportClient.getClass().getMethod("addTransportAddress", Class.forName("org.elasticsearch.common.transport.TransportAddress"));
            addTransportAddressMethod.invoke(transportClient, transportAddressClassConstructor.newInstance(InetAddress.getByName(address), port));
        } catch (Exception e) {
                throw SeedException.wrap(e, ElasticSearchErrorCode.CANNOT_CREATE_ADDRESS)
                        .put("AddressType", "InetSocketTransportAddress");
        }
    }
}
