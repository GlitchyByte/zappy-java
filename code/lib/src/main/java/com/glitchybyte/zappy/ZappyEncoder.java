// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Zappy encoder.
 *
 * <p>It uses base64 as the message encoding, but the internal bytes are compressed.
 */
public final class ZappyEncoder extends ZappyBase64StringEncoder {

    private static final long MAX_DECIMAL = 0x7fffffff;

    private final Map<Integer, Map<Integer, byte[]>> contractions;
    private ByteBuffer zappyBuffer = GByteBuffer.create();

    /**
     * Creates a Zappy encoder.
     *
     * @param contractions The contractions used for aiding compression.
     */
    public ZappyEncoder(final Map<Integer, Map<Integer, byte[]>> contractions) {
        this.contractions = contractions;
    }

    /**
     * Turns a string into a Zappy compressed string.
     *
     * @param str A string.
     * @return A Zappy compressed string.
     */
    public String encode(final String str) {
        final byte[] bytes = stringToCompressedBytes(str);
        return bytesToBase64Alphabet(bytes);
    }

    private byte[] stringToCompressedBytes(final String str) {
        final byte[] source = str.getBytes(StandardCharsets.UTF_8);
        zappyBuffer.clear();
        int index = 0;
        while (index < source.length) {
            index += addNextToken(source, index);
        }
        return GByteBuffer.toByteArray(zappyBuffer);
    }

    private int addNextToken(final byte[] source, final int index) {
        int used;
        // Contraction.
        used = addContractionToken(source, index);
        if (used > 0) {
            return used;
        }
        // Repeated.
        used = addRepeatToken(source, index);
        if (used > 0) {
            return used;
        }
        final byte b = source[index];
        // Check for (0..9] || [A..F] || [a..f]
        if (((b > 0x30) && (b <= 0x39)) || ((b >= 0x41) && (b <= 0x46)) || ((b >= 0x61) && (b <= 0x66))) {
            // Unsigned integer.
            used = addUnsignedIntegerToken(source, index);
            if (used > 0) {
                return used;
            }
        }
        if ((b & 0x80) == 0) {
            // ASCII. Take as-is.
            return addAsciiToken(source, index);
        }
        // Non-ASCII. Take as-is as a group.
        return addBlobToken(source, index);
    }

    private int addContractionToken(final byte[] source, final int index) {
        for (int tableId = 16; tableId >= 0; --tableId) {
            final Map<Integer, byte[]> lookup = contractions.get(tableId);
            if (lookup == null) {
                continue;
            }
            final int lookupIndex = findLookupIndex(lookup, source, index);
            if (lookupIndex == -1) {
                continue;
            }
            if (tableId == 0) {
                zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 1);
                final int token = 0xe0 | lookupIndex;
                zappyBuffer.put((byte) token);
            } else {
                zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 2);
                final int token = 0xf0 | (tableId - 1);
                zappyBuffer.put((byte) token);
                zappyBuffer.put((byte) lookupIndex);
            }
            return lookup.get(lookupIndex).length;
        }
        return 0;
    }

    private int findLookupIndex(final Map<Integer, byte[]> lookup, final byte[] source, final int index) {
        for (final var entry: lookup.entrySet()) {
            final int lookupIndex = entry.getKey();
            final byte[] bytes = entry.getValue();
            if (bytes.length > (source.length - index)) {
                continue;
            }
            boolean found = true;
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] != source[index + i]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return lookupIndex;
            }
        }
        return -1;
    }

    private int addRepeatToken(final byte[] source, final int index) {
        final int maxRepeatCount = 0x1f;
        int count = 1;
        final byte value = source[index];
        while (count < maxRepeatCount) {
            final int walker = index + count;
            if (walker >= source.length) {
                break;
            }
            final byte b = source[walker];
            if (value != b) {
                break;
            }
            ++count;
        }
        if (count < 3) {
            return 0;
        }
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 2);
        final int token = 0xa0 | count;
        zappyBuffer.put((byte) token);
        zappyBuffer.put(value);
        return count;
    }

    private boolean isUppercaseHexDigit(final byte b) {
        return (b >= 0x41) && (b <= 0x46); // [A..F]
    }

    private boolean isLowercaseHexDigit(final byte b) {
        return (b >= 0x61) && (b <= 0x66); // [a..f]
    }

    private boolean isDigit(final byte b) {
        return (b >= 0x30) && (b <= 0x39); // [0..9]
    }

    private int addUnsignedIntegerToken(final byte[] source, final int index) {
        // Collect up to 10 decimal or 8 hex.
        int count = 1;
        byte b = source[index];
        boolean isUppercase = isUppercaseHexDigit(b);
        boolean isHex = isUppercase || isLowercaseHexDigit(b);
        while ((isHex && (count < 8)) || (!isHex && (count < 10))) {
        final int walker = index + count;
            if (walker >= source.length) {
                break;
            }
            b = source[walker];
            if (isDigit(b)) {
                ++count;
                continue;
            }
            if (isHex) {
                if (isUppercase && isUppercaseHexDigit(b)) {
                    ++count;
                    continue;
                }
                if (!isUppercase && isLowercaseHexDigit(b)) {
                    ++count;
                    continue;
                }
                break;
            }
            if (isUppercaseHexDigit(b)) {
                if (count >= 8) {
                    break;
                }
                isHex = true;
                isUppercase = true;
                ++count;
                continue;
            }
            if (isLowercaseHexDigit(b)) {
                if (count >= 8) {
                    break;
                }
                isHex = true;
                ++count;
                continue;
            }
            break;
        }
        return isHex ?
                addHexadecimalToken(source, index, count, isUppercase) :
                addDecimalToken(source, index, count);
    }

    private int addDecimalToken(final byte[] source, final int index, final int count) {
        // FIXME JS BUG: Can't have an unsigned 32bit int, if bit 31 is set JS interprets it as a negative number.
        //  So we'll only encode numbers up to 31 bits long. Though the problem only shows when decoding, we prevent
        //  encoding so we don't manifest the bug later.
        int digit = 0;
        long value = 0;
        while (digit < count) {
            final byte b = source[index + digit];
            final long newValue = (value * 10) + (b - 0x30);
            if (newValue > MAX_DECIMAL) {
                break;
            }
            value = newValue;
            ++digit;
        }
        // Minimum encoding size is 2 bytes (token + UInt8). So we do not encode numbers under 100 which are
        // 2 bytes to start with in ASCII.
        if (value < 100) {
            return 0;
        }
        final int byteCount;
        if (value > 0xffff) {
            byteCount = 4;
        } else if (value > 0xff) {
            byteCount = 2;
        } else {
            byteCount = 1;
        }
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 1 + byteCount);
        final int token = 0xc0 | byteCount;
        zappyBuffer.put((byte) token);
        switch (byteCount) {
            case 4 -> zappyBuffer.putInt((int) value);
            case 2 -> zappyBuffer.putShort((short) value);
            default -> zappyBuffer.put((byte) value);
        }
        return count;
    }

    private int addHexadecimalToken(final byte[] source, final int index, final int count, final boolean isUppercase) {
        // FIXME JS BUG: Can't have an unsigned 32bit int, if bit 31 is set JS interprets it as a negative number.
        //  So we'll only encode numbers up to 31 bits long. Though the problem only shows when decoding, we prevent
        //  encoding so we don't manifest the bug later.
        int digit = 0;
        long value = 0;
        while (digit < count) {
            final byte b = source[index + digit];
            final int digitValue;
            if (isDigit(b)) {
                digitValue = b - 0x30;
            } else if (isUppercase) {
                digitValue = b - 0x37; // 0x0a + (byte - 0x41)
            } else {
                digitValue = b - 0x57; // 0x0a + (byte - 0x61)
            }
            if ((value & 0x08000000) != 0) {
                break;
            }
            value = (value * 0x10) | digitValue;
            ++digit;
        }
        // Minimum encoding size is 3 bytes (token + UInt16). So we do not encode hex numbers under 0x1000 which are
        // 3 bytes to start with in ASCII.
        if (value < 0x1000) {
            return 0;
        }
        final int byteCount = value > 0xffff ? 4 : 2;
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 1 + byteCount);
        final int token = (isUppercase ? 0xd0 : 0xd8) | byteCount;
        zappyBuffer.put((byte) token);
        if (byteCount == 4) {
            zappyBuffer.putInt((int) value);
        } else {
            zappyBuffer.putShort((short) value);
        }
        return digit;
    }

    private int addAsciiToken(final byte[] source, final int index) {
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 1);
        zappyBuffer.put(source[index]);
        return 1;
    }

    private int addBlobToken(final byte[] source, final int index) {
        final int maxBlobSize = 0x1f;
        int count = 1;
        while (count < maxBlobSize) {
            final int walker = index + count;
            if (walker >= source.length) {
                break;
            }
            final byte b = source[walker];
            if ((b & 0x80) == 0) {
                break;
            }
            ++count;
        }
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 1 + count);
        final int token = 0x80 | count;
        zappyBuffer.put((byte) token);
        zappyBuffer.put(source, index, count);
        return count;
    }
}
