/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import javax.annotation.concurrent.Immutable;

/**
 * A connection from a profile Sample to a trace Span.
 * @see "pprofextended.proto::Link"
 */
@Immutable
public interface LinkData {

  /**
   * A unique identifier of a trace that this linked span is part of.
   * The ID is a 16-byte array.
   */
  @SuppressWarnings("mutable")
  byte[] getTraceId();

  /**
   * A unique identifier for the linked span.
   * The ID is an 8-byte array.
   */
  @SuppressWarnings("mutable")
  byte[] getSpanId();
}
