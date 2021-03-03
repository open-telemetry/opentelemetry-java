/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;
import javax.annotation.concurrent.Immutable;

/** TODO: javadoc. */
@AutoValue
@Immutable
public abstract class View {
  public abstract AggregatorFactory getAggregatorFactory();

  public abstract LabelsProcessorFactory getLabelsProcessorFactory();

  public static ViewBuilder builder() {
    return new ViewBuilder();
  }

  static View create(
      AggregatorFactory aggregatorFactory, LabelsProcessorFactory labelsProcessorFactory) {
    return new AutoValue_View(aggregatorFactory, labelsProcessorFactory);
  }
}
