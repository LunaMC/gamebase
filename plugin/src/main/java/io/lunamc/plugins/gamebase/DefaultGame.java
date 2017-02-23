/*
 *  Copyright 2017 LunaMC.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.lunamc.plugins.gamebase;

import io.lunamc.common.host.VirtualHost;
import io.lunamc.common.network.AuthorizedConnection;
import io.lunamc.common.status.StatusProvider;
import io.lunamc.gamebase.Game;
import io.lunamc.plugins.gamebase.block.DefaultBlockRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultGame implements Game {

    private final DefaultBlockRegistry blockRegistry = new DefaultBlockRegistry();
    private final Set<AuthorizedConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<AuthorizedConnection> connectionsView = Collections.unmodifiableSet(connections);
    private final Set<VirtualHost> handledVirtualHosts = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicReference<StatusProvider> statusProvider = new AtomicReference<>();

    @Override
    public DefaultBlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    @Override
    public Collection<AuthorizedConnection> getConnections() {
        return connectionsView;
    }

    public void addConnection(AuthorizedConnection connection) {
        connections.add(connection);
    }

    public void removeConnection(AuthorizedConnection connection) {
        connections.remove(connection);
    }

    @Override
    public Collection<VirtualHost> getHandledVirtualHosts() {
        return handledVirtualHosts;
    }

    @Override
    public StatusProvider getStatusProvider() {
        return statusProvider.get();
    }

    @Override
    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider.set(statusProvider);
    }
}
