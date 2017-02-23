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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class LazyHolderTest {

    @Test
    public void testLazyHolder() {
        AtomicBoolean called = new AtomicBoolean(false);
        LazyHolder<String> test = LazyHolder.create(() -> {
            if (!called.compareAndSet(false, true))
                Assert.fail("Supplier called two times");
            return "Test";
        });

        Assert.assertFalse(called.get());
        Assert.assertEquals("Test", test.getValue());
        Assert.assertTrue(called.get());
        Assert.assertEquals("Test", test.getValue());
    }
}
