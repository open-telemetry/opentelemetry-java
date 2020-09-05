/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.trace;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.trace.Event;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a generic messaging event. This class can represent messaging happened in
 * any layer, especially higher application layer. Thus, it can be used when recording events in
 * pipeline works, in-process bidirectional streams and batch processing.
 *
 * <p>It requires a {@link Type type} and a message id that serves to uniquely identify each
 * message. It can optionally have information about the message size.
 *
 * @since 0.1.0
 */
@Immutable
public final class MessageEvent implements Event {

  private static final String EVENT_NAME = "message";
  private static final String TYPE = "message.type";
  private static final String ID = "message.id";
  private static final String COMPRESSED_SIZE = "message.compressed_size";
  private static final String UNCOMPRESSED_SIZE = "message.uncompressed_size";

  /**
   * Available types for a {@code MessageEvent}.
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
    RECEIVED,
  }

  private static final AttributeValue sentAttributeValue =
      AttributeValue.stringAttributeValue(Type.SENT.name());
  private static final AttributeValue receivedAttributeValue =
      AttributeValue.stringAttributeValue(Type.RECEIVED.name());
  private static final AttributeValue zeroAttributeValue = AttributeValue.longAttributeValue(0);

  private final Attributes attributes;

  /**
   * Returns a {@code MessageEvent} with the desired values.
   *
   * @param type designates whether this is a send or receive message.
   * @param messageId serves to uniquely identify each message.
   * @param uncompressedSize represents the uncompressed size in bytes of this message. If not
   *     available use 0.
   * @param compressedSize represents the compressed size in bytes of this message. If not available
   *     use 0.
   * @return a {@code MessageEvent} with the desired values.
   * @throws NullPointerException if {@code type} is {@code null}.
   * @since 0.1.0
   */
  public static MessageEvent create(
      Type type, long messageId, long uncompressedSize, long compressedSize) {
    Attributes.Builder attributeBuilder = Attributes.newBuilder();
    attributeBuilder.setAttribute(
        TYPE, type == Type.SENT ? sentAttributeValue : receivedAttributeValue);
    attributeBuilder.setAttribute(
        ID, messageId == 0 ? zeroAttributeValue : AttributeValue.longAttributeValue(messageId));
    attributeBuilder.setAttribute(
        UNCOMPRESSED_SIZE,
        uncompressedSize == 0
            ? zeroAttributeValue
            : AttributeValue.longAttributeValue(uncompressedSize));
    attributeBuilder.setAttribute(
        COMPRESSED_SIZE,
        compressedSize == 0
            ? zeroAttributeValue
            : AttributeValue.longAttributeValue(compressedSize));
    return new MessageEvent(attributeBuilder.build());
  }

  @Override
  public String getName() {
    return EVENT_NAME;
  }

  @Override
  public Attributes getAttributes() {
    return attributes;
  }

  private MessageEvent(Attributes attributes) {
    this.attributes = attributes;
  }
}
