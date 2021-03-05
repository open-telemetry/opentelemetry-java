/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of {@link LinkData}. */
@AutoValue
@Immutable
abstract class ImmutableLinkData implements LinkData {
  private static final Attributes DEFAULT_ATTRIBUTE_COLLECTION = Attributes.empty();
  private static final int DEFAULT_ATTRIBUTE_COUNT = 0;

  static LinkData create(SpanContext spanContext) {
    return new AutoValue_ImmutableLinkData(
        spanContext, DEFAULT_ATTRIBUTE_COLLECTION, DEFAULT_ATTRIBUTE_COUNT);
  }

  static LinkData create(SpanContext spanContext, Attributes attributes) {
    return new AutoValue_ImmutableLinkData(spanContext, attributes, attributes.size());
  }

  static LinkData create(SpanContext spanContext, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_ImmutableLinkData(spanContext, attributes, totalAttributeCount);
  }

  ImmutableLinkData() {}
}
