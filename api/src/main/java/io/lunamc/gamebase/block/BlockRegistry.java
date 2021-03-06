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

package io.lunamc.gamebase.block;

import java.util.Optional;

public interface BlockRegistry {

    default Optional<Block> getBlockByName(String name) {
        return getBlockByName(name, 0);
    }

    Optional<Block> getBlockByName(String name, int variant);

    Block register(String name, int paletteId);

    int getGlobalPaletteSize();
}
