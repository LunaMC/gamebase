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

package io.lunamc.gamebase.world;

import io.lunamc.gamebase.math.vector.Vector3f;

import java.util.Objects;

public interface MutableFuzzyLocation extends FuzzyLocation {

    LocationUpdater getLocationUpdater();

    interface LocationUpdater {

        default void update(FuzzyLocation location) {
            Objects.requireNonNull(location, "location must be not null");
            update(location.getWorld(), location.getPosition(), location.getYaw(), location.getPitch());
        }

        void update(World world, Vector3f position, float yaw, float pitch);

        void unset();
    }
}
