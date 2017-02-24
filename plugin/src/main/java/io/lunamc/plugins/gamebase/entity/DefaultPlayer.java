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

package io.lunamc.plugins.gamebase.entity;

import io.lunamc.common.network.AuthorizedConnection;
import io.lunamc.common.utils.UuidUtils;
import io.lunamc.gamebase.entity.Player;
import io.lunamc.gamebase.math.vector.Vector3f;
import io.lunamc.gamebase.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

public class DefaultPlayer implements Player {

    private final LocationHolder locationHolder = new LocationHolder();
    private final AuthorizedConnection authorizedConnection;
    private final int entityId;
    private UUID uuid;

    public DefaultPlayer(int entityId, AuthorizedConnection authorizedConnection) {
        this.entityId = entityId;
        this.authorizedConnection = authorizedConnection;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public Optional<World> getWorld() {
        return locationHolder.getWorld();
    }

    @Override
    public Optional<Vector3f> getPosition() {
        return locationHolder.getPosition();
    }

    @Override
    public LocationUpdater getLocationUpdater() {
        return locationHolder;
    }

    @Override
    public AuthorizedConnection getConnection() {
        return authorizedConnection;
    }

    @Override
    public UUID getUniqueID() {
        if (uuid == null)
            uuid = UuidUtils.parseUuid(getConnection().getProfile().getId());
        return uuid;
    }

    @Override
    public String getUsername() {
        return getConnection().getProfile().getName();
    }

    private class LocationHolder implements LocationUpdater {

        private final StampedLock lock = new StampedLock();
        private World world;
        private Vector3f position;

        private Optional<World> getWorld() {
            long stamp = lock.readLock();
            try {
                return Optional.ofNullable(world);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        private Optional<Vector3f> getPosition() {
            long stamp = lock.readLock();
            try {
                return Optional.ofNullable(position);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        @Override
        public void update(World world, Vector3f position) {
            long stamp = lock.writeLock();
            try {
                this.world = world;
                this.position = position;
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }
}
