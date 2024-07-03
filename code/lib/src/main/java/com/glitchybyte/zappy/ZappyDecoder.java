// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.util.Map;

/**
 * Zappy decoder.
 *
 * <p>It uses base64 as the message encoding, but the internal bytes are compressed.
 */
public final class ZappyDecoder extends ZappyBase64StringDecoder {

    private final Map<Integer, Map<Integer, byte[]>> contractions;

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
     * @return Expanded string or null.
     * @throws ZappyParseException if it's an invalid Zappy string.
     */
    public String decode(final String str) throws ZappyParseException {
        // TODO: Convert this!
        return null;
    }
}
