/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of {@link SpanData.Link}. */
@Immutable
@AutoValue
abstract class ImmutableLink implements SpanData.Link {

  private static final Attributes DEFAULT_ATTRIBUTE_COLLECTION = Attributes.empty();
  private static final int DEFAULT_ATTRIBUTE_COUNT = 0;

  static ImmutableLink create(SpanContext spanContext) {
    return new AutoValue_ImmutableLink(
        spanContext, DEFAULT_ATTRIBUTE_COLLECTION, DEFAULT_ATTRIBUTE_COUNT);
  }

  static ImmutableLink create(SpanContext spanContext, Attributes attributes) {
    return new AutoValue_ImmutableLink(spanContext, attributes, attributes.size());
  }

  static ImmutableLink create(
      SpanContext spanContext, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_ImmutableLink(spanContext, attributes, totalAttributeCount);
  }

  ImmutableLink() {}
}
