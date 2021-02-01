/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/**
 * Type of {@link Span}. Can be used to specify additional relationships between spans in addition
 * to a parent/child relationship.
 */
public enum SpanKind {
  /** Default value. Indicates that the span is used internally. */
  INTERNAL,

  /** Indicates that the span covers server-side handling of an RPC or other remote request. */
  SERVER,

  /**
   * Indicates that the span covers the client-side wrapper around an RPC or other remote request.
   */
  CLIENT,

  /**
   * Indicates that the span describes producer sending a message to a broker. Unlike client and
   * server, there is no direct critical path latency relationship between producer and consumer
   * spans.
   */
  PRODUCER,

  /**
   * Indicates that the span describes consumer receiving a message from a broker. Unlike client and
   * server, there is no direct critical path latency relationship between producer and consumer
   * spans.
   */
  CONSUMER
}
