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

import io.lunamc.gamebase.Game;
import io.lunamc.plugins.gamebase.block.DefaultBlockRegistry;

import java.util.Objects;

public class DefaultGame implements Game {

    private final DefaultBlockRegistry blockRegistry = new DefaultBlockRegistry();
    private final MinecraftVersion version;

    public DefaultGame(MinecraftVersion version) {
        this.version = Objects.requireNonNull(version, "version must not be null");
    }

    public void initialize() {
        Initializer.initializeBlocks(this, version);
    }

    @Override
    public DefaultBlockRegistry getBlockRegistry() {
        return blockRegistry;
    }
}
