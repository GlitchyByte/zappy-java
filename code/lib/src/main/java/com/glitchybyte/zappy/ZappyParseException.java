// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

/**
 * Exception for when a parsing error occurs.
 */
public class ZappyParseException extends Exception {

    /**
     * Creates a parse exception with a message.
     *
     * @param message Message to attach to the exception.
     */
    public ZappyParseException(final String message) {
        super(message);
    }
}
