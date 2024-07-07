// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Base64 string decoder.
 */
public class ZappyBase64StringDecoder {

    private ByteBuffer base64Buffer = GByteBuffer.create();

    /**
     * Creates a Zappy base64 decoder.
     */
    public ZappyBase64StringDecoder() {
        // No-op.
    }

    /**
     * Decodes a base64 string.
     *
     * <p>Expects encoding with "-" and "_", and no padding.
     *
     * @param str Base64 string.
     * @return The decoded string.
     * @throws ZappyParseException if it's an invalid base64 string.
     */
    public String base64StringDecode(final String str) throws ZappyParseException {
        final byte[] bytes = base64AlphabetToBytes(str);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private int base64ToByte(final char chByte) throws ZappyParseException {
        if ((chByte >= 65) && (chByte <= 90)) {
            return chByte - 65;
        }
        if ((chByte >= 97) && (chByte <= 122)) {
            return chByte - 71;
        }
        if ((chByte >= 48) && (chByte <= 57)) {
            return chByte + 4;
        }
        if (chByte == 45) {
            return 62;
        }
        if (chByte == 95) {
            return 63;
        }
        throw new ZappyParseException("Invalid base64 character!");
    }

    /**
     * Converts a base64 string into decoded bytes.
     *
     * @param str Base64 string.
     * @return Decoded bytes.
     * @throws ZappyParseException if it's an invalid base64 string.
     */
    protected byte[] base64AlphabetToBytes(final String str) throws ZappyParseException {
        // Base64 decode.
        // We have 4 6-bit bytes. Make 3 bytes out of them.
        final int strLength = str.length();
        base64Buffer.clear();
        if ((strLength & 3) == 1) {
            throw new ZappyParseException("Illegal number of bytes!");
        }
        int start = 0;
        while (start < strLength) {
            base64Buffer = GByteBuffer.ensureCapacityForMoreBytes(base64Buffer, 3);
            final int count = Math.min(4, strLength - start);
            final int b0 = base64ToByte(str.charAt(start));
            final int b1 = base64ToByte(str.charAt(start + 1));
            final int d0 = (b0 << 2) | (b1 >> 4);
            base64Buffer.put((byte) d0);
            if (count == 4) {
                final int b2 = base64ToByte(str.charAt(start + 2));
                final int d1 = ((b1 & 0x0f) << 4) | (b2 >> 2);
                final int b3 = base64ToByte(str.charAt(start + 3));
                final int d2 = ((b2 & 0x03) << 6) | b3;
                final int word = (d2 << 8) | d1;
                base64Buffer.putShort((short) word);
            } else if (count == 3) {
                final int b2 = base64ToByte(str.charAt(start + 2));
                final int d1 = ((b1 & 0x0f) << 4) | (b2 >> 2);
                base64Buffer.put((byte) d1);
            }
            start += count;
        }
        return GByteBuffer.toByteArray(base64Buffer);
    }
}
