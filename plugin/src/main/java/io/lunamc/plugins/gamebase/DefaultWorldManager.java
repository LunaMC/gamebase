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

import io.lunamc.gamebase.WorldManager;
import io.lunamc.gamebase.entity.Player;
import io.lunamc.gamebase.world.FuzzyLocation;
import io.lunamc.plugins.gamebase.utils.LazyHolder;

import java.util.Objects;

public class DefaultWorldManager implements WorldManager {

    private final LazyHolder<FuzzyLocation> spawn;

    public DefaultWorldManager(LazyHolder<FuzzyLocation> spawn) {
        this.spawn = Objects.requireNonNull(spawn, "spawn must not be null");
    }

    @Override
    public FuzzyLocation decideSpawnLocation(Player player) {
        return spawn.getValue();
    }
}
