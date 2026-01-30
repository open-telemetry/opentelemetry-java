/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import java.util.Collections;
import javax.annotation.Nullable;

final class MetricReaderInstrumentation {

  private final DoubleHistogram collectionDuration;
  private final Attributes standardAttrs;

  MetricReaderInstrumentation(ComponentId componentId, MeterProvider meterProvider) {
    Meter meter = meterProvider.get("io.opentelemetry.sdk.metrics");

    standardAttrs =
        Attributes.of(
            SemConvAttributes.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvAttributes.OTEL_COMPONENT_NAME,
            componentId.getComponentName());

    collectionDuration =
        meter
            .histogramBuilder("otel.sdk.metric_reader.collection.duration")
            .setUnit("s")
            .setDescription("The duration of the collect operation of the metric reader.")
            .setExplicitBucketBoundariesAdvice(Collections.emptyList())
            .build();
  }

  void recordCollection(double seconds, @Nullable String error) {
    Attributes attrs = standardAttrs;
    if (error != null) {
      attrs = attrs.toBuilder().put(SemConvAttributes.ERROR_TYPE, error).build();
    }

    collectionDuration.record(seconds, attrs);
  }
}
