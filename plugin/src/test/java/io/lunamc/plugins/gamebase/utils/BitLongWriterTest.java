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

public class BitLongWriterTest {

    @Test
    public void testWrite() {
        long[] buf = new long[3];
        BitLongWriter writer = new BitLongWriter(buf, 13);

        // Example values from http://wiki.vg/index.php?title=SMP_Map_Format&oldid=8381#Example except the two most
        // significant bits of the last value
        writer.write(0b0000000100000);
        writer.write(0b0000000110000);
        writer.write(0b0000000110000);
        writer.write(0b0000000110001);
        writer.write(0b0000000010000);
        writer.write(0b0000000010000);
        writer.write(0b0000000010011);
        writer.write(0b0000011010000);
        writer.write(0b0000011010000);
        writer.write(0b1100000010000);

        Assert.assertEquals(
                0b000000010000_0000000110001_0000000110000_0000000110000_0000000100000L,
                buf[0]
        );
        Assert.assertEquals(
                0b00000010000_0000011010000_0000011010000_0000000010011_0000000010000_0L,
                buf[1]
        );
        Assert.assertEquals(
                0b00000000000000000000000000000000000000000000000000000000000000_11L,
                buf[2]
        );
    }
}
