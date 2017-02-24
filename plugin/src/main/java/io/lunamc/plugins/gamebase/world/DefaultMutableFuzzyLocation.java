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
import io.lunamc.gamebase.world.MutableFuzzyLocation;
import io.lunamc.gamebase.world.World;

import java.util.concurrent.locks.StampedLock;

public class DefaultMutableFuzzyLocation implements MutableFuzzyLocation {

    private final StampedLock lock = new StampedLock();
    private final LocationHolder locationUpdater = new LocationHolder();
    private World world;
    private Vector3f position;
    private float yaw;
    private float pitch;
    private volatile boolean available;

    @Override
    public World getWorld() {
        long stamp = lock.readLock();
        try {
            return world;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Vector3f getPosition() {
        long stamp = lock.readLock();
        try {
            return position;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public float getYaw() {
        long stamp = lock.readLock();
        try {
            return yaw;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public float getPitch() {
        long stamp = lock.readLock();
        try {
            return pitch;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public LocationUpdater getLocationUpdater() {
        return locationUpdater;
    }

    private class LocationHolder implements LocationUpdater {

        @Override
        public void update(World world, Vector3f position, float yaw, float pitch) {
            long stamp = lock.writeLock();
            try {
                DefaultMutableFuzzyLocation.this.world = world;
                DefaultMutableFuzzyLocation.this.position = position;
                DefaultMutableFuzzyLocation.this.yaw = yaw;
                DefaultMutableFuzzyLocation.this.pitch = pitch;
                available = true;
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        @Override
        public void unset() {
            long stamp = lock.writeLock();
            try {
                DefaultMutableFuzzyLocation.this.world = null;
                DefaultMutableFuzzyLocation.this.position = null;
                DefaultMutableFuzzyLocation.this.yaw = 0f;
                DefaultMutableFuzzyLocation.this.pitch = 0f;
                available = false;
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }
}
