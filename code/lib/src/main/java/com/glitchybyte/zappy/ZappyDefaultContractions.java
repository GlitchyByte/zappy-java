// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.util.Arrays;
import java.util.Map;

/**
 * Default contractions for Zappy.
 */
public final class ZappyDefaultContractions {

    /**
     * Default contractions for Zappy.
     *
     * <p>It is highly recommended developers using Zappy add their own contraction tables,
     * or even replace them completely.
     *
     * <p>These defaults are optimized for json messages and URLs.
     */
    public static final Map<Integer, String[]> defaultContractions = Map.of(
            0, new String[] { // Up to 16 entries.
                    "null",
                    "true",
                    "false",
                    "https://",
                    "0x",
                    "{\"",
                    "\"}",
                    "\":",
                    "\":\"",
                    ",\"",
                    "\",\"",
                    "\":[",
                    "\":[\"",
                    "\":[{",
                    "}]",
                    "]}"
            },
            16, new String[] { // Up to 256 entries.
                    "localhost",
                    "127.0.0.1",
                    "http://",
                    "ws://",
                    "://",
                    ".com",
                    ".org",
                    ".net",
                    ".edu",
                    ".io",
                    ".dev",
                    ".gg"
            });

    static {
        defaultContractions.forEach((key, value) -> {
            Arrays.sort(value, (a, b) -> b.length() - a.length());
        });
    }

    private ZappyDefaultContractions() {
        // Hiding constructor.
    }
}
