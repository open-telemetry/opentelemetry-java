/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of the {@link SpanData.Event}. */
@Immutable
@AutoValue
abstract class ImmutableEvent implements SpanData.Event {

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @return a new immutable {@code Event<T>}
   */
  static ImmutableEvent create(long epochNanos, String name, Attributes attributes) {
    return new AutoValue_ImmutableEvent(name, attributes, epochNanos, attributes.size());
  }

  static ImmutableEvent create(
      long epochNanos, String name, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_ImmutableEvent(name, attributes, epochNanos, totalAttributeCount);
  }

  ImmutableEvent() {}
}
