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

package openconsensus.trace;

import com.google.auto.value.AutoValue;
import openconsensus.common.Timestamp;
import openconsensus.internal.Utils;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a network event. It requires a {@link Type type} and a message id that
 * serves to uniquely identify each network message. It can optionally can have information about
 * the kernel time and message size.
 *
 * @deprecated Use {@link MessageEvent}.
 * @since 0.1.0
 */
@Immutable
@AutoValue
@AutoValue.CopyAnnotations
@Deprecated
public abstract class NetworkEvent extends BaseMessageEvent {
  /**
   * Available types for a {@code NetworkEvent}.
   *
   * @since 0.1.0
   */
  public enum Type {
    /**
     * When the message was sent.
     *
     * @since 0.1.0
     */
    SENT,
    /**
     * When the message was received.
     *
     * @since 0.1.0
     */
    RECV,
  }

  /**
   * Returns a new {@link Builder} with default values.
   *
   * @param type designates whether this is a network send or receive message.
   * @param messageId serves to uniquely identify each network message.
   * @return a new {@code Builder} with default values.
   * @throws NullPointerException if {@code type} is {@code null}.
   * @since 0.1.0
   */
  public static Builder builder(Type type, long messageId) {
    return new AutoValue_NetworkEvent.Builder()
        .setType(Utils.checkNotNull(type, "type"))
        .setMessageId(messageId)
        // We need to set a value for the message size because the autovalue requires all
        // primitives to be initialized.
        .setUncompressedMessageSize(0)
        .setCompressedMessageSize(0);
  }

  /**
   * Returns the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   * set.
   *
   * @return the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   *     set.
   * @since 0.1.0
   */
  @Nullable
  public abstract Timestamp getKernelTimestamp();

  /**
   * Returns the type of the {@code NetworkEvent}.
   *
   * @return the type of the {@code NetworkEvent}.
   * @since 0.1.0
   */
  public abstract Type getType();

  /**
   * Returns the message id argument that serves to uniquely identify each network message.
   *
   * @return the message id of the {@code NetworkEvent}.
   * @since 0.1.0
   */
  public abstract long getMessageId();

  /**
   * Returns the uncompressed size in bytes of the {@code NetworkEvent}.
   *
   * @return the uncompressed size in bytes of the {@code NetworkEvent}.
   * @since 0.1.0
   */
  public abstract long getUncompressedMessageSize();

  /**
   * Returns the compressed size in bytes of the {@code NetworkEvent}.
   *
   * @return the compressed size in bytes of the {@code NetworkEvent}.
   * @since 0.1.0
   */
  public abstract long getCompressedMessageSize();

  /**
   * Returns the uncompressed size in bytes of the {@code NetworkEvent}.
   *
   * @deprecated Use {@link #getUncompressedMessageSize}.
   * @return the uncompressed size in bytes of the {@code NetworkEvent}.
   * @since 0.1.0
   */
  @Deprecated
  public long getMessageSize() {
    return getUncompressedMessageSize();
  }

  /**
   * Builder class for {@link NetworkEvent}.
   *
   * @deprecated {@link NetworkEvent} is deprecated. Please use {@link MessageEvent} and its builder
   *     {@link MessageEvent.Builder}.
   * @since 0.1.0
   */
  @AutoValue.Builder
  @Deprecated
  public abstract static class Builder {
    // Package protected methods because these values are mandatory and set only in the
    // NetworkEvent#builder() function.
    abstract Builder setType(Type type);

    abstract Builder setMessageId(long messageId);

    /**
     * Sets the kernel timestamp.
     *
     * @param kernelTimestamp The kernel timestamp of the event.
     * @return this.
     * @since 0.1.0
     */
    public abstract Builder setKernelTimestamp(@Nullable Timestamp kernelTimestamp);

    /**
     * Sets the uncompressed message size.
     *
     * @deprecated Use {@link #setUncompressedMessageSize}.
     * @param messageSize represents the uncompressed size in bytes of this message.
     * @return this.
     * @since 0.1.0
     */
    @Deprecated
    public Builder setMessageSize(long messageSize) {
      return setUncompressedMessageSize(messageSize);
    }

    /**
     * Sets the uncompressed message size.
     *
     * @param uncompressedMessageSize represents the uncompressed size in bytes of this message.
     * @return this.
     * @since 0.1.0
     */
    public abstract Builder setUncompressedMessageSize(long uncompressedMessageSize);

    /**
     * Sets the compressed message size.
     *
     * @param compressedMessageSize represents the compressed size in bytes of this message.
     * @return this.
     * @since 0.1.0
     */
    public abstract Builder setCompressedMessageSize(long compressedMessageSize);

    /**
     * Builds and returns a {@code NetworkEvent} with the desired values.
     *
     * @return a {@code NetworkEvent} with the desired values.
     * @since 0.1.0
     */
    public abstract NetworkEvent build();

    Builder() {}
  }

  NetworkEvent() {}
}
