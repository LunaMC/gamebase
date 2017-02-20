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

import org.junit.Assert;
import org.junit.Test;

public class HalfByteChunkStorageTest {

    @Test
    public void testSetGet() {
        HalfByteChunkStorage storage = new HalfByteChunkStorage();
        storage.set(0, 0, 0, (byte) 4);
        storage.set(11, 12, 13, (byte) 8);
        storage.set(12, 13, 14, (byte) 15);
        Assert.assertEquals(4, storage.get(0, 0, 0));
        Assert.assertEquals(8, storage.get(11, 12, 13));
        Assert.assertEquals(15, storage.get(12, 13, 14));
    }

    @Test
    public void testFill() {
        HalfByteChunkStorage storage = new HalfByteChunkStorage();
        storage.fill(0b1111);
        for (byte b : storage.array())
            Assert.assertEquals((byte) 0b11111111, b);
    }
}
