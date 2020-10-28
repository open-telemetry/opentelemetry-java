/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.trace;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a generic messaging event. This class can represent messaging happened in
 * any layer, especially higher application layer. Thus, it can be used when recording events in
 * pipeline works, in-process bidirectional streams and batch processing.
 *
 * <p>It requires a {@link Type type} and a message id that serves to uniquely identify each
 * message. It can optionally have information about the message size.
 */
@Immutable
public final class MessageEvent {

  private static final String EVENT_NAME = "message";
  private static final AttributeKey<String> TYPE = stringKey("message.type");
  private static final AttributeKey<Long> ID = longKey("message.id");
  private static final AttributeKey<Long> COMPRESSED_SIZE = longKey("message.compressed_size");
  private static final AttributeKey<Long> UNCOMPRESSED_SIZE = longKey("message.uncompressed_size");

  /** Available types for a {@code MessageEvent}. */
  public enum Type {
    /** When the message was sent. */
    SENT,
    /** When the message was received. */
    RECEIVED,
  }

  /**
   * Records a {@code MessageEvent} with the desired values to the given Span.
   *
   * @param span the span to record the {@code MessageEvent} to.
   * @param type designates whether this is a send or receive message.
   * @param messageId serves to uniquely identify each message.
   * @param uncompressedSize represents the uncompressed size in bytes of this message. If not
   *     available use 0.
   * @param compressedSize represents the compressed size in bytes of this message. If not available
   *     use 0.
   */
  public static void record(
      Span span, Type type, long messageId, long uncompressedSize, long compressedSize) {
    Attributes.Builder attributeBuilder = Attributes.builder();
    attributeBuilder.put(TYPE, type == Type.SENT ? Type.SENT.name() : Type.RECEIVED.name());
    attributeBuilder.put(ID, messageId);
    attributeBuilder.put(UNCOMPRESSED_SIZE, uncompressedSize);
    attributeBuilder.put(COMPRESSED_SIZE, compressedSize);
    span.addEvent(EVENT_NAME, attributeBuilder.build());
  }

  private MessageEvent() {}
}
