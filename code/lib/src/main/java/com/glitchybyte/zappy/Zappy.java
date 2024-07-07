// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encoding and decoding compressed web text for transport.
 *
 * <p>It uses base64 as the message encoding, but the internal bytes are compressed.
 */
public final class Zappy {

    private final ZappyEncoder encoder;
    private final ZappyDecoder decoder;

    /**
     * Creates a Zappy object ready to encode and decode messages.
     *
     * @param source The contraction source used for aiding compression. These will be overlaid
     *          on the default contractions that favors json. Whole tables are replaced in the
     *          overlay process, not individual items within a table.
     *          If source is null (and it has to be explicit by design), then only the default
     *          contractions are used. It is highly recommended users of this class add their
     *          own contractions.
     */
    public Zappy(final Map<Integer, String[]> source) {
        if (source != null) {
            for (final int key: source.keySet()) {
                if ((key < 0) || (key > 16)) {
                    throw new IllegalArgumentException("Invalid tableId: " + key);
                }
            }
        }
        final Map<Integer, Map<Integer, byte[]>> contractions = new HashMap<>();
        // Layer contraction tables.
        for (int tableId = 0; tableId <= 16; ++tableId) {
            final String[] list;
            if ((source == null) || (!source.containsKey(tableId))) {
                list = ZappyDefaultContractions.defaultContractions.get(tableId);
            } else {
                final String[] sourceList = source.get(tableId);
                list = Arrays.stream(sourceList).sorted((a, b) -> b.length() - a.length()).toArray(String[]::new);
            }
            if (list == null) {
                continue;
            }
            final Map<Integer, byte[]> lookup = createLookup(tableId, list);
            contractions.put(tableId, lookup);
        }
        // Create encoder and decoder.
        encoder = new ZappyEncoder(contractions);
        decoder = new ZappyDecoder(contractions);
    }

    private Map<Integer, byte[]> createLookup(final int tableId, final String[] list) {
        // Convert to bytes for contraction tables.
        final Map<Integer, byte[]> lookup = new HashMap<>();
        for (final String entry: list) {
          final byte[] bytes = entry.getBytes(StandardCharsets.UTF_8);
            if (tableId == 0) {
                if (bytes.length <= 1) {
                    throw new IllegalArgumentException("Contraction is smaller than encoding: [1-byte] " + entry);
                }
            } else if (bytes.length <= 2) {
                throw new IllegalArgumentException("Contraction is smaller than encoding: [2-byte] " + entry);
            }
            lookup.put(lookup.size(), bytes);
        }
        return lookup;
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
        return encoder.base64StringEncode(str);
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
        return decoder.base64StringDecode(str);
    }

    /**
     * Turns a string into a Zappy compressed string.
     *
     * @param str A string.
     * @return A Zappy compressed string.
     */
    public String encode(final String str) {
        return encoder.encode(str);
    }

    /**
     * Turns a Zappy compressed string into a string.
     *
     * @param str A Zappy compressed string.
     * @return Expanded string.
     * @throws ZappyParseException if it's an invalid Zappy string.
     */
    public String decode(final String str) throws ZappyParseException {
        return decoder.decode(str);
    }
}
