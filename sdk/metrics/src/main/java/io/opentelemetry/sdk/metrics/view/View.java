/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Configuration for a view. */
@AutoValue
@Immutable
public abstract class View {

  /** Configuration for what to output. */
  protected abstract MetricOutputConfiguration getOutput();

  /** The Instrument selection criteria. */
  public abstract InstrumentSelectionCriteria getSelection();

  /** The `name` of the View (optional). If not provided, the Instrument `name` will be used. */
  @Nullable
  public final String getName() {
    return getOutput().getName();
  }

  /**
   * The `aggregation` (optional) to be used. If not provided, the default aggregation (based on the
   * type of the Instrument) will be applied.
   */
  public final AggregatorFactory<?> getAggregator(InstrumentDescriptor instrument) {
    return getOutput().getAggregator(instrument);
  }

  /**
   * A processor which takes in the attributes oof a measurement and determines the resulting set of
   * attributes.
   */
  public final AttributesProcessor getAttributesProcessor() {
    return getOutput().getAttributesProcessor();
  }

  /** Summons a new builder for views. */
  public static Builder builder() {
    return new AutoValue_View.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setSelection(InstrumentSelectionCriteria critera);

    public abstract Builder setOutput(MetricOutputConfiguration output);

    public abstract View build();
  }
}
