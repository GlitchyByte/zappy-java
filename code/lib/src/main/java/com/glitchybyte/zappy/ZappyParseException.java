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

    /**
     * Creates a parse exception from another exception in the process.
     *
     * @param cause The exception that triggered the problem.
     */
    public ZappyParseException(final Throwable cause) {
        super(cause);
    }
}
