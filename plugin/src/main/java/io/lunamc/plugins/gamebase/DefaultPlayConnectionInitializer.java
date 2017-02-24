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

import io.lunamc.common.network.AuthorizedConnection;
import io.lunamc.common.play.PlayConnectionInitializer;
import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.GameManager;
import io.lunamc.gamebase.entity.Player;
import io.lunamc.platform.service.ServiceRegistration;
import io.lunamc.platform.service.di.PreferredConstructor;
import io.lunamc.plugins.gamebase.entity.DefaultPlayer;
import io.lunamc.plugins.gamebase.exception.UnassignableGameException;
import io.lunamc.plugins.netty.handler.ProtocolLoginHandler;
import io.lunamc.plugins.netty.network.NettyAuthorizedConnection;

public class DefaultPlayConnectionInitializer implements PlayConnectionInitializer {

    private static final String HANDLER_NAME = "play-handler";

    private final ServiceRegistration<GameManager> gameManager;

    @PreferredConstructor
    public DefaultPlayConnectionInitializer(ServiceRegistration<GameManager> gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void initialize(AuthorizedConnection connection) {
        if (!(connection instanceof NettyAuthorizedConnection))
            throw new UnsupportedOperationException("connect must be provided by luna-netty plugin");
        Game game = gameManager.requireInstance().getGameForVirtualHost(connection.getVirtualHost()).orElseThrow(UnassignableGameException::new);
        NettyAuthorizedConnection castedConnection = (NettyAuthorizedConnection) connection;
        Player player = new DefaultPlayer(game.getEntityIdAllocator().obtain(), connection);
        game.getPlayers().add(player);
        castedConnection.channel().pipeline()
                .replace(ProtocolLoginHandler.HANDLER_NAME, HANDLER_NAME, new DefaultPlayHandler(game, player, castedConnection));
    }
}
