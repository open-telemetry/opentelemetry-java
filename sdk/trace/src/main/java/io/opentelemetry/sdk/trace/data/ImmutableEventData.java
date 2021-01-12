/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of the {@link EventData}. */
@Immutable
@AutoValue
abstract class ImmutableEventData implements EventData {

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @return a new immutable {@code Event<T>}
   */
  static EventData create(long epochNanos, String name, Attributes attributes) {
    return create(epochNanos, name, attributes, attributes.size());
  }

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @param totalAttributeCount the total number of attributes recorded for the {@code Event}.
   * @return a new immutable {@code Event<T>}
   */
  static EventData create(
      long epochNanos, String name, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_ImmutableEventData(name, attributes, epochNanos, totalAttributeCount);
  }

  ImmutableEventData() {}
}
