// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

/**
 * Common utilities.
 */
public final class GUtils {

    /**
     * Converts a byte that is supposed to be unsigned into an int,
     * in order to preserve the full byte range as a positive number.
     *
     * @param n byte value.
     * @return int value.
     */
    public static int byteToInt(final byte n) {
        return n < 0 ? 0x100 + n : n;
    }

    /**
     * Converts a short that is supposed to be unsigned into an int,
     * in order to preserve the full short range as a positive number.
     *
     * @param n short value.
     * @return int value.
     */
    public static int shortToInt(final short n) {
        return n < 0 ? 0x10000 + n : n;
    }

    private GUtils() {
        // Hiding constructor.
    }
}
