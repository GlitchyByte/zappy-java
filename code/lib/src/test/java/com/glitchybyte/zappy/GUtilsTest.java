// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GUtilsTest {

    @Test
    void byteToInt() {
        final byte b = (byte) 0xff;
        assertNotEquals(0xff, b);
        final int value = GUtils.byteToInt(b);
        assertEquals(0xff, value);
    }

    @Test
    void shortToInt() {
        final short s = (short) 0xffff;
        assertNotEquals(0xffff, s);
        final int value = GUtils.shortToInt(s);
        assertEquals(0xffff, value);
    }
}
