/*
 * Copyright 2019, OpenConsensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openconsensus.opencensusshim.trace;

import java.util.Arrays;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import openconsensus.opencensusshim.internal.DefaultVisibilityForTesting;
import openconsensus.opencensusshim.internal.Utils;

/**
 * A class that represents global trace options. These options are propagated to all child {@link
 * openconsensus.opencensusshim.trace.Span spans}. These determine features such as whether a {@code
 * Span} should be traced. It is implemented as a bitmask.
 *
 * @since 0.1.0
 */
@Immutable
public final class TraceOptions {
  // Default options. Nothing set.
  private static final byte DEFAULT_OPTIONS = 0;
  // Bit to represent whether trace is sampled or not.
  private static final byte IS_SAMPLED = 0x1;

  /**
   * The size in bytes of the {@code TraceOptions}.
   *
   * @since 0.1.0
   */
  public static final int SIZE = 1;

  private static final int BASE16_SIZE = 2 * SIZE;

  /**
   * The default {@code TraceOptions}.
   *
   * @since 0.1.0
   */
  public static final TraceOptions DEFAULT = fromByte(DEFAULT_OPTIONS);

  // The set of enabled features is determined by all the enabled bits.
  private final byte options;

  // Creates a new {@code TraceOptions} with the given options.
  private TraceOptions(byte options) {
    this.options = options;
  }

  /**
   * Returns a {@code TraceOptions} whose representation is {@code src}.
   *
   * @param src the byte representation of the {@code TraceOptions}.
   * @return a {@code TraceOptions} whose representation is {@code src}.
   * @since 0.1.0
   */
  public static TraceOptions fromByte(byte src) {
    // TODO(bdrutu): OPTIMIZATION: Cache all the 256 possible objects and return from the cache.
    return new TraceOptions(src);
  }

  /**
   * Returns a {@code TraceOption} built from a lowercase base16 representation.
   *
   * @param src the lowercase base16 representation.
   * @param srcOffset the offset in the buffer where the representation of the {@code TraceOptions}
   *     begins.
   * @return a {@code TraceOption} built from a lowercase base16 representation.
   * @throws NullPointerException if {@code src} is null.
   * @throws IllegalArgumentException if {@code src.length} is not {@code 2 * TraceOption.SIZE} OR
   *     if the {@code str} has invalid characters.
   * @since 0.1.0
   */
  public static TraceOptions fromLowerBase16(CharSequence src, int srcOffset) {
    return new TraceOptions(BigendianEncoding.byteFromBase16String(src, srcOffset));
  }

  /**
   * Returns the one byte representation of the {@code TraceOptions}.
   *
   * @return the one byte representation of the {@code TraceOptions}.
   * @since 0.1.0
   */
  public byte getByte() {
    return options;
  }

  /**
   * Copies the byte representations of the {@code TraceOptions} into the {@code dest} beginning at
   * the {@code destOffset} offset.
   *
   * <p>Equivalent with (but faster because it avoids any new allocations):
   *
   * <pre>{@code
   * System.arraycopy(getBytes(), 0, dest, destOffset, TraceOptions.SIZE);
   * }</pre>
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws NullPointerException if {@code dest} is null.
   * @throws IndexOutOfBoundsException if {@code destOffset+TraceOptions.SIZE} is greater than
   *     {@code dest.length}.
   * @since 0.1.0
   */
  public void copyBytesTo(byte[] dest, int destOffset) {
    Utils.checkIndex(destOffset, dest.length);
    dest[destOffset] = options;
  }

  /**
   * Copies the lowercase base16 representations of the {@code TraceId} into the {@code dest}
   * beginning at the {@code destOffset} offset.
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws IndexOutOfBoundsException if {@code destOffset + 2} is greater than {@code
   *     dest.length}.
   * @since 0.1.0
   */
  public void copyLowerBase16To(char[] dest, int destOffset) {
    BigendianEncoding.byteToBase16String(options, dest, destOffset);
  }

  /**
   * Returns the lowercase base16 encoding of this {@code TraceOptions}.
   *
   * @return the lowercase base16 encoding of this {@code TraceOptions}.
   * @since 0.1.0
   */
  public String toLowerBase16() {
    char[] chars = new char[BASE16_SIZE];
    copyLowerBase16To(chars, 0);
    return new String(chars);
  }

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   * @since 0.1.0
   */
  public static Builder builder() {
    return new Builder(DEFAULT_OPTIONS);
  }

  /**
   * Returns a new {@link Builder} with all given options set.
   *
   * @param traceOptions the given options set.
   * @return a new {@code Builder} with all given options set.
   * @since 0.1.0
   */
  public static Builder builder(TraceOptions traceOptions) {
    return new Builder(traceOptions.options);
  }

  /**
   * Returns a boolean indicating whether this {@code Span} is part of a sampled trace and data
   * should be exported to a persistent store.
   *
   * @return a boolean indicating whether the trace is sampled.
   * @since 0.1.0
   */
  public boolean isSampled() {
    return hasOption(IS_SAMPLED);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof TraceOptions)) {
      return false;
    }

    TraceOptions that = (TraceOptions) obj;
    return options == that.options;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new byte[] {options});
  }

  @Override
  public String toString() {
    return "TraceOptions{sampled=" + isSampled() + "}";
  }

  /**
   * Builder class for {@link TraceOptions}.
   *
   * @since 0.1.0
   */
  public static final class Builder {
    private byte options;

    private Builder(byte options) {
      this.options = options;
    }

    /**
     * Sets the sampling bit in the options.
     *
     * @param isSampled the sampling bit.
     * @return this.
     * @since 0.1.0
     */
    public Builder setIsSampled(boolean isSampled) {
      if (isSampled) {
        options = (byte) (options | IS_SAMPLED);
      } else {
        options = (byte) (options & ~IS_SAMPLED);
      }
      return this;
    }

    /**
     * Builds and returns a {@code TraceOptions} with the desired options.
     *
     * @return a {@code TraceOptions} with the desired options.
     * @since 0.1.0
     */
    public TraceOptions build() {
      return fromByte(options);
    }
  }

  // Returns the current set of options bitmask.
  @DefaultVisibilityForTesting
  byte getOptions() {
    return options;
  }

  private boolean hasOption(int mask) {
    return (this.options & mask) != 0;
  }
}
