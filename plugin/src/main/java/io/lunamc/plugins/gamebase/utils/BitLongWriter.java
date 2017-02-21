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

public class BitLongWriter {

    private final long[] buf;
    private final int bitsPerValue;
    private byte valueIndex;
    private int bufIndex;

    public BitLongWriter(long[] buf, int bitsPerValue) {
        this.buf = buf;
        this.bitsPerValue = bitsPerValue;
    }

    public void write(long value) {
        int bitsToWrite = bitsPerValue;
        while (bitsToWrite > 0) {
            int writableBits = Math.min(bitsToWrite, Long.SIZE - valueIndex);
            buf[bufIndex] = (value << valueIndex) | buf[bufIndex];
            value >>= writableBits;
            bitsToWrite -= writableBits;
            valueIndex += writableBits;
            if (valueIndex == Long.SIZE) {
                valueIndex = 0;
                bufIndex++;
            }
        }
    }
}
