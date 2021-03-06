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
import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.GameManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGameManager implements GameManager {

    private final Set<Game> games = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void addGame(Game game) {
        games.add(game);
    }

    @Override
    public Optional<Game> getGameForVirtualHost(VirtualHost virtualHost) {
        for (Game game : getGames()) {
            if (game.getHandledVirtualHosts().contains(virtualHost))
                return Optional.of(game);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Game> getGames() {
        return games;
    }
}
