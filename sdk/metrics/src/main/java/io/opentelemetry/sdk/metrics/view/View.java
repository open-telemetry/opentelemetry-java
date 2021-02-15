/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class View {
  public abstract AggregatorFactory getAggregatorFactory();

  public abstract LabelsProcessorFactory getLabelsProcessorFactory();

  public static Builder builder() {
    return new AutoValue_View.Builder().setLabelsProcessorFactory(LabelsProcessorFactory.noop());
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setAggregatorFactory(AggregatorFactory aggregatorFactory);

    public abstract Builder setLabelsProcessorFactory(
        LabelsProcessorFactory labelsProcessorFactory);

    public abstract View build();
  }
}
