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
import io.opentelemetry.sdk.common.internal.SemConvConstants;
import java.util.Collections;
import javax.annotation.Nullable;

final class MetricReaderInstrumentation {

  private final DoubleHistogram collectionDuration;
  private final Attributes standardAttrs;

  MetricReaderInstrumentation(ComponentId componentId, MeterProvider meterProvider) {
    Meter meter = meterProvider.get("io.opentelemetry.sdk.metrics");

    standardAttrs =
        Attributes.of(
            SemConvConstants.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvConstants.OTEL_COMPONENT_NAME,
            componentId.getComponentName());

    collectionDuration =
        meter
            .histogramBuilder(SemConvConstants.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_NAME)
            .setUnit(SemConvConstants.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_UNIT)
            .setDescription(SemConvConstants.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_DESCRIPTION)
            .setExplicitBucketBoundariesAdvice(Collections.emptyList())
            .build();
  }

  void recordCollection(double seconds, @Nullable String error) {
    Attributes attrs = standardAttrs;
    if (error != null) {
      attrs = attrs.toBuilder().put(SemConvConstants.ERROR_TYPE, error).build();
    }

    collectionDuration.record(seconds, attrs);
  }
}
