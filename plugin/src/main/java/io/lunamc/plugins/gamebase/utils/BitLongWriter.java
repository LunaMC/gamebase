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
