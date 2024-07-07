// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.nio.charset.StandardCharsets;

/**
 * Base64 string encoder.
 *
 * <p>Alphabet includes '-' and '_'. Does not produce padding characters.
 */
public class ZappyBase64StringEncoder {

    private static final String base64Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

    /**
     * Creates a Zappy base64 encoder.
     */
    public ZappyBase64StringEncoder() {
        // No-op.
    }

    /**
     * Encodes a string into a base64 string.
     *
     * <p>Encodes with "-" and "_", and no padding.
     *
     * @param str Original string to encode.
     * @return A base64 string.
     */
    public String base64StringEncode(final String str) {
        final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return bytesToBase64Alphabet(bytes);
    }

    /**
     * Converts raw bytes into a base64 string.
     *
     * @param bytes Raw bytes.
     * @return A base64 string.
     */
    protected String bytesToBase64Alphabet(final byte[] bytes) {
        // Base64 encode.
        // We have 3 bytes. Make 4 6-bit bytes out of them.
        final int bytesLength = bytes.length;
        final StringBuilder sb = new StringBuilder();
        int start = 0;
        while (start < bytesLength) {
            final int count = Math.min(3, bytesLength - start);
            final int b0 = GUtils.byteToInt(bytes[start]);
            final int e0 = b0 >> 2;
            sb.append(base64Alphabet.charAt(e0));
            if (count == 3) {
                final int b1 = GUtils.byteToInt(bytes[start + 1]);
                final int b2 = GUtils.byteToInt(bytes[start + 2]);
                final int e1 = ((b0 & 0x03) << 4) | (b1 >> 4);
                sb.append(base64Alphabet.charAt(e1));
                final int e2 = ((b1 & 0x0f) << 2) | (b2 >> 6);
                sb.append(base64Alphabet.charAt(e2));
                final int e3 = b2 & 0x3f;
                sb.append(base64Alphabet.charAt(e3));
            } else if (count == 2) {
                final int b1 = GUtils.byteToInt(bytes[start + 1]);
                final int e1 = ((b0 & 0x03) << 4) | (b1 >> 4);
                sb.append(base64Alphabet.charAt(e1));
                final int e2 = (b1 & 0x0f) << 2;
                sb.append(base64Alphabet.charAt(e2));
            } else {
                final int e1 = (b0 & 0x03) << 4;
                sb.append(base64Alphabet.charAt(e1));
            }
            start += count;
        }
        return sb.toString();
    }
}
