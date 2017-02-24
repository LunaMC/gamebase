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

package io.lunamc.plugins.gamebase.world;

import io.lunamc.gamebase.math.vector.Vector3f;
import io.lunamc.gamebase.world.FuzzyLocation;
import io.lunamc.gamebase.world.World;

import java.util.Objects;

public class DefaultFuzzyLocation implements FuzzyLocation {

    private final World world;
    private final Vector3f position;
    private final float yaw;
    private final float pitch;

    public DefaultFuzzyLocation(World world, Vector3f position, float yaw, float pitch) {
        this.world = Objects.requireNonNull(world, "world must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public float getPitch() {
        return pitch;
    }
}
