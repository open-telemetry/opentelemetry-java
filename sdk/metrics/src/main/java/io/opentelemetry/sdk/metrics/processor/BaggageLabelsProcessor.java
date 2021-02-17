/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.api.metrics.common.LabelsBuilder;
import io.opentelemetry.context.Context;

/**
 * A label processor which extracts labels from {@link io.opentelemetry.api.baggage.Baggage}.
 * Delegates actual extraction implementation to {@link BaggageMetricsLabelsExtractor}
 */
public final class BaggageLabelsProcessor implements LabelsProcessor {
  private final BaggageMetricsLabelsExtractor baggageMetricsLabelsExtractor;

  public BaggageLabelsProcessor(BaggageMetricsLabelsExtractor baggageMetricsLabelsExtractor) {
    this.baggageMetricsLabelsExtractor = baggageMetricsLabelsExtractor;
  }

  @Override
  public Labels onLabelsBound(Context ctx, Labels labels) {
    LabelsBuilder labelsBuilder = labels.toBuilder();
    baggageMetricsLabelsExtractor.fromBaggage(ctx).forEach(labelsBuilder::put);

    return labelsBuilder.build();
  }
}
