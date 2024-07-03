// Copyright 2024 GlitchyByte
// SPDX-License-Identifier: Apache-2.0

package com.glitchybyte.zappy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Helper for ByteBuffer.
 *
 * <p>Ensures all storage references are little-endian.
 */
public final class GByteBuffer {

    /**
     * Creates a default empty buffer.
     *
     * @return A ByteBuffer object.
     */
    public static ByteBuffer create() {
        return ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Ensures the buffer can hold the given number of bytes.
     *
     * @param buffer Source ByteBuffer.
     * @param capacity Desired minimum buffer capacity.
     * @return The ByteBuffer object that should be used instead of the given one. It may be the same.
     */
    public static ByteBuffer ensureCapacity(final ByteBuffer buffer, final int capacity) {
        if (buffer.capacity() >= capacity) {
            return buffer;
        }
        final ByteBuffer newBuffer = ByteBuffer.allocate((int) Math.floor(capacity * 1.5)).order(ByteOrder.LITTLE_ENDIAN);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Ensures the buffer can hold the given extra number of bytes.
     *
     * @param buffer Source ByteBuffer.
     * @param countToAdd Desired added number of bytes.
     * @return The ByteBuffer object that should be used instead of the given one. It may be the same.
     */
    public static ByteBuffer ensureCapacityForMoreBytes(final ByteBuffer buffer, final int countToAdd) {
        return ensureCapacity(buffer, buffer.position() + countToAdd);
    }

    /**
     * Returns the valid bytes from the ByteBuffer as an array.
     *
     * @param buffer Source ByteBuffer.
     * @return Valid bytes from the ByteBuffer as an array.
     */
    public static byte[] toByteArray(final ByteBuffer buffer) {
        buffer.flip();
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private GByteBuffer() {
        // Hiding constructor.
    }
}
