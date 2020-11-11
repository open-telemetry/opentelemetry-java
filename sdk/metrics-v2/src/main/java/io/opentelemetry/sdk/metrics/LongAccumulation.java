/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class LongAccumulation implements Accumulation {
  static LongAccumulation create(long startTime, long value) {
    return new AutoValue_LongAccumulation(startTime, MetricData.Type.MONOTONIC_LONG, value);
  }

  abstract long value();

  @Override
  public MetricData.Point convertToPoint(long epochNanos, Labels labels) {
    return MetricData.LongPoint.create(getStartTime(), epochNanos, labels, value());
  }
}
