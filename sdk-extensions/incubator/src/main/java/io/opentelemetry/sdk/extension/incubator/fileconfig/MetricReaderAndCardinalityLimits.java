/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import javax.annotation.Nullable;

@AutoValue
abstract class MetricReaderAndCardinalityLimits {

  static MetricReaderAndCardinalityLimits create(
      MetricReader metricReader, @Nullable CardinalityLimitSelector cardinalityLimitSelector) {
    return new AutoValue_MetricReaderAndCardinalityLimits(metricReader, cardinalityLimitSelector);
  }

  abstract MetricReader getMetricReader();

  @Nullable
  abstract CardinalityLimitSelector getCardinalityLimitsSelector();
}
