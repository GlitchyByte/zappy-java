// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Zappy decoder.
 *
 * <p>It uses base64 as the message encoding, but the internal bytes are compressed.
 */
public final class ZappyDecoder extends ZappyBase64StringDecoder {

    private final Map<Integer, Map<Integer, byte[]>> contractions;
    private ByteBuffer zappyBuffer = GByteBuffer.create();

    /**
     * Creates a Zappy decoder.
     *
     * @param contractions The contractions used for aiding compression.
     */
    public ZappyDecoder(final Map<Integer, Map<Integer, byte[]>> contractions) {
        this.contractions = contractions;
    }

    /**
     * Turns a Zappy compressed string into a string.
     *
     * @param str A Zappy compressed string.
     * @return Expanded string.
     * @throws ZappyParseException if it's an invalid Zappy string.
     */
    public String decode(final String str) throws ZappyParseException {
        byte[] bytes = base64AlphabetToBytes(str);
        try {
            bytes = stringToDecompressedBytes(bytes);
        } catch (final RuntimeException e) {
            throw new ZappyParseException(e);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private byte[] stringToDecompressedBytes(final byte[] bytes) throws ZappyParseException {
        final ByteBuffer source = GByteBuffer.createWrapped(bytes);
        zappyBuffer.clear();
        while (source.hasRemaining()) {
            final byte b = source.get();
            resolveNextToken(b, source);
        }
        return GByteBuffer.toByteArray(zappyBuffer);
    }

    private void resolveNextToken(final byte b, final ByteBuffer source) throws ZappyParseException {
        if ((b & 0x80) == 0) {
            // ASCII character. Take as-is.
            resolveAsciiToken(b);
            return;
        }
        if ((b & 0x40) == 0) {
            // Level 1 compressed instruction.
            if ((b & 0x20) == 0) {
                // Blob. Take as-is as a group.
                resolveBlobToken(b, source);
                return;
            }
            // Repeated character.
            resolveRepeatToken(b, source);
            return;
        }
        // Level 2 compressed instruction.
        if ((b & 0x20) == 0) {
            // Unsigned integer.
            if ((b & 0x10) == 0) {
                // Decimal integer.
                resolveDecimalToken(b, source);
                return;
            }
            // Hexadecimal integer.
            final boolean isUppercase = (b & 0x08) == 0;
            resolveHexadecimalToken(b, source, isUppercase);
            return;
        }
        // Contraction lookup.
        resolveContractionToken(b, source);
    }

    private void resolveAsciiToken(final byte b) {
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, 1);
        zappyBuffer.put(b);
    }

    private void resolveBlobToken(final byte b, final ByteBuffer source) {
        final int count = b & 0x1f;
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, count);
        zappyBuffer.put(zappyBuffer.position(), source, source.position(), count);
        zappyBuffer.position(zappyBuffer.position() + count);
        source.position(source.position() + count);
    }

    private void resolveRepeatToken(final byte b, final ByteBuffer source) {
        final int count = b & 0x1f;
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, count);
        final byte sourceB = source.get();
        for (int i = 0; i < count; ++i) {
            zappyBuffer.put(sourceB);
        }
    }

    private void resolveDecimalToken(final byte b, final ByteBuffer source) throws ZappyParseException {
        final int count = b & 0x0f;
        final int value = switch (count) {
            case 1 -> GUtils.byteToInt(source.get());
            case 2 -> GUtils.shortToInt(source.getShort());
            case 4 -> source.getInt();
            default -> throw new ZappyParseException("Invalid byte count: " + count);
        };
        final byte[] digits = Integer.toString(value).getBytes(StandardCharsets.UTF_8);
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, digits.length);
        zappyBuffer.put(digits);
    }

    private void resolveHexadecimalToken(final byte b, final ByteBuffer source, final boolean isUppercase) throws ZappyParseException {
        final int count = b & 0x07;
        final int value = switch (count) {
            case 2 -> GUtils.shortToInt(source.getShort());
            case 4 -> source.getInt();
            default -> throw new ZappyParseException("Invalid byte count: " + count);
        };
        String hex = Integer.toHexString(value);
        if (isUppercase) {
            hex = hex.toUpperCase(Locale.US);
        }
        final byte[] digits = hex.getBytes(StandardCharsets.UTF_8);
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, digits.length);
        zappyBuffer.put(digits);
    }

    private void resolveContractionToken(final byte b, final ByteBuffer source) throws ZappyParseException {
        int tableId;
        int lookupIndex;
        if ((b & 0x10) == 0) {
            // Fast lookup!
            tableId = 0;
            lookupIndex = b & 0x0f;
        } else {
            tableId = (b & 0x0f) + 1;
            lookupIndex = source.get();
        }
        final Map<Integer, byte[]> lookup = contractions.get(tableId);
        if (lookup == null) {
            throw new ZappyParseException(
                    String.format(Locale.US, "No contractions found [tableId: %d]", tableId));
        }
        final byte[] bytes = lookup.get(lookupIndex);
        if (bytes == null) {
            throw new ZappyParseException(
                    String.format(Locale.US, "Contraction lookup index [%d]:%d not found!", tableId, lookupIndex));
        }
        zappyBuffer = GByteBuffer.ensureCapacityForMoreBytes(zappyBuffer, bytes.length);
        zappyBuffer.put(bytes);
    }
}
