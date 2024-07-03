// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ZappyTest {

    @Test
    void base64EncodeDecode() {
        final Zappy zappy = new Zappy(null);
        final String original = "hello";
        final String encoded = zappy.base64StringEncode(original);
        assertNotEquals(original, encoded);
        final String decoded;
        try {
            decoded = zappy.base64StringDecode(encoded);
        } catch (final ZappyParseException e) {
            fail();
            return;
        }
        assertEquals(original, decoded);
    }

    @Test
    void base64ProperEncodeCheck() {
        final Zappy zappy = new Zappy(null);
        final String encoded = zappy.base64StringEncode("hello");
        assertEquals("aGVsbG8", encoded);
    }

    @Test
    void base64MalformedCheck() {
        final Zappy zappy = new Zappy(null);
        assertThrowsExactly(ZappyParseException.class, () -> zappy.base64StringDecode("aGVsb@8")); // Wrong character.
        assertThrowsExactly(ZappyParseException.class, () -> zappy.base64StringDecode("aGVsbG8c1")); // Wrong length.
    }
}
