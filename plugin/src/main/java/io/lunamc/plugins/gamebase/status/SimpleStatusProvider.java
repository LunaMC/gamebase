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

package io.lunamc.plugins.gamebase.status;

import io.lunamc.common.network.Connection;
import io.lunamc.common.network.DecidedConnection;
import io.lunamc.common.status.BetaStatusResponse;
import io.lunamc.common.status.LegacyStatusResponse;
import io.lunamc.common.status.StaticBetaStatusResponse;
import io.lunamc.common.status.StaticLegacyStatusResponse;
import io.lunamc.common.status.StaticStatusResponse;
import io.lunamc.common.status.StatusProvider;
import io.lunamc.common.status.StatusResponse;
import io.lunamc.common.text.TextComponent;
import io.lunamc.common.text.builder.ComponentBuilderFactory;
import io.lunamc.gamebase.Game;
import io.lunamc.platform.service.ServiceRegistration;
import io.lunamc.plugins.gamebase.utils.LazyHolder;
import io.lunamc.plugins.gamebase.utils.VersionMetaUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class SimpleStatusProvider implements StatusProvider {

    private final Game game;
    private final String motd;
    private final LazyHolder<TextComponent> componentMotd;
    private final int maxPlayers;

    public SimpleStatusProvider(ServiceRegistration<ComponentBuilderFactory> componentBuilderFactory, Game game, String motd, int maxPlayers) {
        Objects.requireNonNull(componentBuilderFactory, "componentBuilderFactory must not be null");
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.motd = Objects.requireNonNull(motd, "motd must not be null");
        this.maxPlayers = maxPlayers;

        componentMotd = LazyHolder.create(() -> componentBuilderFactory.requireInstance().createTextComponentBuilder().text(motd).build());
    }

    @Override
    public StatusResponse createStatusResponse(DecidedConnection connection) {
        Optional<VersionMetaUtils.MinecraftVersion> version = VersionMetaUtils.getVersionByProtocolVersion(connection.getProtocolVersion(), VersionMetaUtils.VersionOrder.MOST_RECENT);
        return new StaticStatusResponse(
                new StaticStatusResponse.StaticVersion(
                        version.map(VersionMetaUtils.MinecraftVersion::getVersionName).orElse("Universal"),
                        connection.getProtocolVersion()
                ),
                new StaticStatusResponse.StaticPlayers(
                        maxPlayers,
                        game.getConnections().size(),
                        Collections.emptyList()
                ),
                componentMotd.getValue(),
                null
        );
    }

    @Override
    public LegacyStatusResponse createLegacy16StatusResponse(DecidedConnection connection) {
        Optional<VersionMetaUtils.MinecraftVersion> version = VersionMetaUtils.getVersionByProtocolVersion(connection.getProtocolVersion(), VersionMetaUtils.VersionOrder.MOST_RECENT);
        return new StaticLegacyStatusResponse(
                connection.getProtocolVersion(),
                version.map(VersionMetaUtils.MinecraftVersion::getVersionName).orElse("Universal"),
                motd,
                game.getConnections().size(),
                maxPlayers
        );
    }

    @Override
    public LegacyStatusResponse createLegacy14StatusResponse(Connection connection) {
        return new StaticLegacyStatusResponse(
                0,
                "Universal",
                motd,
                game.getConnections().size(),
                maxPlayers
        );
    }

    @Override
    public BetaStatusResponse createBetaStatusResponse(Connection connection) {
        return new StaticBetaStatusResponse(
                motd,
                game.getConnections().size(),
                maxPlayers
        );
    }
}
