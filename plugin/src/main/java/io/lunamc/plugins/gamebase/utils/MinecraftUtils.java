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

package io.lunamc.plugins.gamebase.utils;

public class MinecraftUtils {

    private MinecraftUtils() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a utility class and should not be constructed");
    }

    public static int parsePaletteId(String str) {
        int colonIndex = str.indexOf(':');
        int id;
        if (colonIndex < 0) {
            id = Integer.parseInt(str);
            id <<= 4;
        } else {
            id = Integer.parseInt(str.substring(0, colonIndex));
            id <<= 4;
            id |= Integer.parseInt(str.substring(colonIndex + 1)) & 0b1111;
        }
        return id;
    }
}
