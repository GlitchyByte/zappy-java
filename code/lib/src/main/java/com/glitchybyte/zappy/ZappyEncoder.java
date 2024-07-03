// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.util.Map;

/**
 * Zappy encoder.
 *
 * <p>It uses base64 as the message encoding, but the internal bytes are compressed.
 */
public final class ZappyEncoder extends ZappyBase64StringEncoder {

    private final Map<Integer, Map<Integer, byte[]>> contractions;

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
        // TODO: Convert this!
        return null;
    }
}
