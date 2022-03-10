/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import javax.annotation.concurrent.Immutable;

/**
 * An {@link ExemplarData} with {@code double} measurements.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableDoubleExemplarData implements DoubleExemplarData {

  /**
   * Construct a new exemplar.
   *
   * @param filteredAttributes The set of {@link Attributes} not already associated with the {@link
   *     PointData}.
   * @param recordTimeNanos The time when the sample qas recorded in nanoseconds.
   * @param spanContext The associated span context.
   * @param value The value recorded.
   */
  public static DoubleExemplarData create(
      Attributes filteredAttributes, long recordTimeNanos, SpanContext spanContext, double value) {
    return new AutoValue_ImmutableDoubleExemplarData(
        filteredAttributes, recordTimeNanos, spanContext, value);
  }

  ImmutableDoubleExemplarData() {}
}
