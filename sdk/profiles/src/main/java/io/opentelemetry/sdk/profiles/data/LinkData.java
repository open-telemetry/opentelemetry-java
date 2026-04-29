/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import javax.annotation.concurrent.Immutable;

/**
 * A connection from a profile Sample to a trace Span.
 *
 * @see "profiles.proto::Link"
 */
@Immutable
public interface LinkData {

  /**
   * Returns a new LinkData representing an association to the given trace span.
   *
   * @return a new LinkData representing an association to the given trace span.
   */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static LinkData create(String traceId, String spanId) {
    return new AutoValue_ImmutableLinkData(traceId, spanId);
  }

  /**
   * Returns a unique identifier of a trace that this linked span is part of as 32 character
   * lowercase hex String.
   */
  String getTraceId();

  /** Returns a unique identifier for the linked span, as 16 character lowercase hex String. */
  String getSpanId();
}
