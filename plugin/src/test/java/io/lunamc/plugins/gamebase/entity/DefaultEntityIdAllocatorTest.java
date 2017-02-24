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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class DefaultEntityIdAllocatorTest {

    @Test
    public void testEntityIdAllocator() {
        DefaultEntityIdAllocator allocator = new DefaultEntityIdAllocator();
        Set<Integer> ids = new HashSet<>(120);
        for (int i = 0; i < 120; i++) {
            int id = allocator.obtain();
            Assert.assertTrue(id > 0);
            Assert.assertTrue(ids.add(id));
        }

        // Free up ids 1 to 20
        for (int i = 1; i <= 20; i++)
            allocator.free(i);

        // Allocate 2 * 20 ids and assert previous freed ids are allocated
        ids = new HashSet<>(40);
        for (int i = 0; i < 40; i++) {
            int id = allocator.obtain();
            Assert.assertTrue(id > 0);
            Assert.assertTrue(ids.add(id));
        }
        for (int i = 1; i <= 20; i++)
            Assert.assertTrue(ids.contains(i));
    }
}
