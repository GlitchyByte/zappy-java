// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ZappyTest {

    @Test
    void base64EncodeDecode() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "hello";
        final String encoded = zappy.base64StringEncode(original);
        assertNotEquals(original, encoded);
        final String decoded;
        decoded = zappy.base64StringDecode(encoded);
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

    @Test
    void emptyMessage() {
        final Zappy zappy = new Zappy(null);
        final String encoded = zappy.encode("");
        assertEquals("", encoded);
    }

    @Test
    void malformedCheck() {
        final Zappy zappy = new Zappy(null);
        assertThrowsExactly(ZappyParseException.class, () -> zappy.decode("c__"));
    }

    @Test
    void repeatedCharacters() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "wwwwww";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void longRepeatedCharacters() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "o".repeat(0x30);
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void blob() throws ZappyParseException {
        // Blobs are not smaller than simple base64.
        final Zappy zappy = new Zappy(null);
        final String original = "👍☠️✌️";
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void longBlob() throws ZappyParseException {
        // Blobs are not smaller than simple base64.
        final Zappy zappy = new Zappy(null);
        final String original = "👍☠️✌️".repeat(0x30);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void integerLessThan100NoContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "::12";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertEquals(base64Encoded.length(), encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void integer1ByteContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = ":255";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void integer2ByteContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "65535";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void integer4ByteContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "2147483647";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void integerMultiGroupContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "::12345678901";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void integerWithLeadingZeroes() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "00123";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexMixedCaseShouldNotContract() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "2b7CaDe";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertEquals(base64Encoded.length(), encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexUppercaseLessThan0x1000NoContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = ":2B7";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertEquals(base64Encoded.length(), encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexUppercase2ByteContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "8E2A";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexUppercase4ByteContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "::7FFFFFFF";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexUppercaseWithLeadingZeroes() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "0012A0";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexUppercaseLongNumber() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "E0012A0F92CC7";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexLowercaseLessThan0x1000NoContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = ":2b7";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertEquals(base64Encoded.length(), encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexLowercase2ByteContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "8e2a";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexLowercase4ByteContraction() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "::7fffffff";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexLowercaseWithLeadingZeroes() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "0012a0";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexLowercaseLongNumber() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "e0012a0f92cc7";
        final String base64Encoded = zappy.base64StringEncode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < base64Encoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void defaultContractionEncodeDecodeJson() throws ZappyParseException {
        final Zappy zappy = new Zappy(null);
        final String original = "{\"url\":\"https://example.com\",\"emoji\":\"🥸\"}";
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void contractionFastTableUnmodified() throws ZappyParseException {
        final Map<Integer, String[]> contractionSource = Map.of(
                1, new String[] { "hello", "hey" },
                2, new String[] { "banana smoothie" },
                4, new String[] { "ice cream" }
        );
        final Zappy defaultZappy = new Zappy(null);
        final Zappy zappy = new Zappy(contractionSource);
        final String original = "{\"url\":\"https://example.nope\"}"; // Only default 0-contractions.
        final String defaultEncoded = defaultZappy.encode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertEquals(defaultEncoded, encoded);
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void contractionLayeredTables() throws ZappyParseException {
        final Map<Integer, String[]> contractionSource = Map.of(
                1, new String[] { "hello", "hey" },
                2, new String[] { "banana smoothie" },
                4, new String[] { "ice cream" }
        );
        final Zappy defaultZappy = new Zappy(null);
        final Zappy zappy = new Zappy(contractionSource);
        final String original = "{\"msg\":\"hello\",\"dessert\":\"ice cream\"}";
        final String defaultEncoded = defaultZappy.encode(original);
        final String encoded = zappy.encode(original);
        assertNotEquals(original, encoded);
        assertTrue(encoded.length() < defaultEncoded.length());
        final String decoded = zappy.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void contractionPreventInvalidTableId() {
        final Map<Integer, String[]> contractionSource = Map.of(
                1, new String[] { "hello", "hey" },
                2, new String[] { "banana smoothie" },
                30, new String[] { "ice cream" }
        );
        assertThrowsExactly(IllegalArgumentException.class, () -> new Zappy(contractionSource));
    }

    @Test
    void preventContractionThatIsNotLargerThanItsEncoding() {
        final Map<Integer, String[]> contractionSource = Map.of(
                11, new String[] { "hi" } // Encoding would 1 control byte and 1 index byte, which is not smaller.
        );
        assertThrowsExactly(IllegalArgumentException.class, () -> new Zappy(contractionSource));
    }
}
