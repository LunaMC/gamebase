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
import io.lunamc.common.host.VirtualHostManager;
import io.lunamc.common.network.Connection;
import io.lunamc.common.network.DecidedConnection;
import io.lunamc.common.status.BetaStatusResponse;
import io.lunamc.common.status.LegacyStatusResponse;
import io.lunamc.common.status.StatusProvider;
import io.lunamc.common.status.StatusResponse;
import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.GameManager;
import io.lunamc.platform.service.ServiceRegistration;
import io.lunamc.platform.service.di.PreferredConstructor;
import io.lunamc.plugins.gamebase.exception.UnassignableGameException;

import java.util.Objects;

public class DefaultStatusProvider implements StatusProvider {

    private final ServiceRegistration<GameManager> gameManager;
    private final ServiceRegistration<VirtualHostManager> virtualHostManager;

    @PreferredConstructor
    public DefaultStatusProvider(ServiceRegistration<GameManager> gameManager, ServiceRegistration<VirtualHostManager> virtualHostManager) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager must not be null");
        this.virtualHostManager = Objects.requireNonNull(virtualHostManager, "virtualHostManager must not be null");
    }

    @Override
    public StatusResponse createStatusResponse(DecidedConnection connection) {
        return resolveGame(connection.getVirtualHost()).getStatusProvider().createStatusResponse(connection);
    }

    @Override
    public LegacyStatusResponse createLegacy16StatusResponse(DecidedConnection connection) {
        return resolveGame(connection.getVirtualHost()).getStatusProvider().createLegacy16StatusResponse(connection);
    }

    @Override
    public LegacyStatusResponse createLegacy14StatusResponse(Connection connection) {
        return resolveGame().getStatusProvider().createLegacy14StatusResponse(connection);
    }

    @Override
    public BetaStatusResponse createBetaStatusResponse(Connection connection) {
        return resolveGame().getStatusProvider().createBetaStatusResponse(connection);
    }

    private Game resolveGame() {
        return resolveGame(virtualHostManager.requireInstance().getFallbackHost());
    }

    private Game resolveGame(VirtualHost virtualHost) {
        return gameManager.requireInstance().getGameForVirtualHost(virtualHost).orElseThrow(UnassignableGameException::new);
    }
}
