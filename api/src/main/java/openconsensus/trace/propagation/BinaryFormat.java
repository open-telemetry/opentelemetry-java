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

package openconsensus.trace.propagation;

import openconsensus.internal.Utils;
import openconsensus.trace.SpanContext;

/**
 * This is a helper class for {@link SpanContext} propagation on the wire using binary encoding.
 *
 * <p>Example of usage on the client:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracing.getTracer();
 * private static final BinaryFormat binaryFormat =
 *     Tracing.getPropagationComponent().getBinaryFormat();
 * void onSendRequest() {
 *   Span span = tracer.spanBuilder("Sent.MyRequest").startSpan();
 *   try (Scope ss = tracer.withSpan(span)) {
 *     byte[] binaryValue = binaryFormat.toByteArray(tracer.getCurrentContext().context());
 *     // Send the request including the binaryValue and wait for the response.
 *   } finally {
 *     span.end();
 *   }
 * }
 * }</pre>
 *
 * <p>Example of usage on the server:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracing.getTracer();
 * private static final BinaryFormat binaryFormat =
 *     Tracing.getPropagationComponent().getBinaryFormat();
 * void onRequestReceived() {
 *   // Get the binaryValue from the request.
 *   SpanContext spanContext = SpanContext.INVALID;
 *   try {
 *     if (binaryValue != null) {
 *       spanContext = binaryFormat.fromByteArray(binaryValue);
 *     }
 *   } catch (SpanContextParseException e) {
 *     // Maybe log the exception.
 *   }
 *   Span span = tracer.spanBuilderWithRemoteParent("Recv.MyRequest", spanContext).startSpan();
 *   try (Scope ss = tracer.withSpan(span)) {
 *     // Handle request and send response back.
 *   } finally {
 *     span.end();
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
public abstract class BinaryFormat {
  static final NoopBinaryFormat NOOP_BINARY_FORMAT = new NoopBinaryFormat();

  /**
   * Serializes a {@link SpanContext} into a byte array using the binary format.
   *
   * @param spanContext the {@code SpanContext} to serialize.
   * @return the serialized binary value.
   * @throws NullPointerException if the {@code spanContext} is {@code null}.
   * @since 0.1.0
   */
  public abstract byte[] toByteArray(SpanContext spanContext);

  /**
   * Parses the {@link SpanContext} from a byte array using the binary format.
   *
   * @param bytes a binary encoded buffer from which the {@code SpanContext} will be parsed.
   * @return the parsed {@code SpanContext}.
   * @throws NullPointerException if the {@code input} is {@code null}.
   * @throws SpanContextParseException if the version is not supported or the input is invalid
   * @since 0.1.0
   */
  public abstract SpanContext fromByteArray(byte[] bytes) throws SpanContextParseException;

  /**
   * Returns the no-op implementation of the {@code BinaryFormat}.
   *
   * @return the no-op implementation of the {@code BinaryFormat}.
   */
  static BinaryFormat getNoopBinaryFormat() {
    return NOOP_BINARY_FORMAT;
  }

  private static final class NoopBinaryFormat extends BinaryFormat {
    @Override
    public byte[] toByteArray(SpanContext spanContext) {
      Utils.checkNotNull(spanContext, "spanContext");
      return new byte[0];
    }

    @Override
    public SpanContext fromByteArray(byte[] bytes) {
      Utils.checkNotNull(bytes, "bytes");
      return SpanContext.INVALID;
    }

    private NoopBinaryFormat() {}
  }
}
