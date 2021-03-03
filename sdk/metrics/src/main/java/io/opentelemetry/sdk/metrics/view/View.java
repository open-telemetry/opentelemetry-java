/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import javax.annotation.concurrent.Immutable;

/** TODO: javadoc. */
@AutoValue
@Immutable
public abstract class View {
  public abstract AggregatorFactory getAggregatorFactory();

  public static ViewBuilder builder() {
    return new ViewBuilder();
  }

  static View create(AggregatorFactory aggregatorFactory) {
    return new AutoValue_View(aggregatorFactory);
  }
}
