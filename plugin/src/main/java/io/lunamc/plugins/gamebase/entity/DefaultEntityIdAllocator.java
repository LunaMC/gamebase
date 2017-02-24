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

import io.lunamc.gamebase.entity.EntityIdAllocator;
import org.eclipse.collections.api.stack.primitive.MutableIntStack;
import org.eclipse.collections.impl.stack.mutable.primitive.IntArrayStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEntityIdAllocator implements EntityIdAllocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityIdAllocator.class);
    private static final int GROW = 100;

    private final MutableIntStack ids = new IntArrayStack();
    private int counter = 1;

    @Override
    public synchronized void free(int id) {
        if (id < 1)
            throw new IllegalArgumentException("id must be greater or equals 1");
        if (ids.contains(id))
            throw new IllegalArgumentException("id " + id + " already freed");
        ids.push(id);
    }

    @Override
    public synchronized int obtain() {
        if (ids.isEmpty())
            grow();
        return ids.pop();
    }

    private void grow() {
        if (counter + GROW < 0)
            throw new IndexOutOfBoundsException("DefaultEntityIdAllocator exceeded");
        for (int i = 0; i < GROW; i++)
            ids.push(counter++);
        LOGGER.debug("DefaultEntityIdAllocator grown for {} items. Counter is now {} and {} ids are available", GROW, counter, ids.size());
    }
}
