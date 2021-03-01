/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class View {
  public abstract AggregatorFactory getAggregatorFactory();

  public static Builder builder() {
    return new AutoValue_View.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setAggregatorFactory(AggregatorFactory aggregatorFactory);

    public abstract View build();
  }
}
