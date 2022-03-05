/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.View;
import javax.annotation.concurrent.Immutable;

/**
 * Describes a metric that will be output.
 *
 * <p>Provides equality/identity semantics for detecting duplicate metrics of incompatible.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class MetricDescriptor {

  /**
   * Constructs a metric descriptor with no instrument and default view.
   *
   * <p>Used for testing + empty-storage only.
   */
  public static MetricDescriptor create(String name, String description, String unit) {
    return new AutoValue_MetricDescriptor(
        name,
        description,
        View.builder().build(),
        InstrumentDescriptor.create(
            name, description, unit, InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.DOUBLE));
  }

  /** Constructs a metric descriptor for a given View + instrument. */
  public static MetricDescriptor create(View view, InstrumentDescriptor instrument) {
    String name = (view.getName() == null) ? instrument.getName() : view.getName();
    String description =
        (view.getDescription() == null) ? instrument.getDescription() : view.getDescription();
    return new AutoValue_MetricDescriptor(name, description, view, instrument);
  }

  /**
   * The name of the descriptor, equal to {@link View#getName()} if not null, else {@link
   * InstrumentDescriptor#getName()}.
   */
  public abstract String getName();

  /**
   * The description of the descriptor, equal to {@link View#getDescription()} if not null, else
   * {@link InstrumentDescriptor#getDescription()}.
   */
  public abstract String getDescription();

  /** The view that lead to the creation of this metric. */
  public abstract View getSourceView();

  /** The instrument which lead to the creation of this metric. */
  public abstract InstrumentDescriptor getSourceInstrument();

  /** The {@link Aggregation#aggregationName(Aggregation)} of the view aggregation. */
  public String getAggregationName() {
    return Aggregation.aggregationName(getSourceView().getAggregation());
  }

  @Memoized
  @Override
  public abstract int hashCode();

  /**
   * Returns true if another metric descriptor is compatible with this one.
   *
   * <p>A metric descriptor is compatible with another if the following are true:
   *
   * <ul>
   *   <li>{@link #getName()} is equal
   *   <li>{@link #getDescription()} is equal
   *   <li>{@link #getAggregationName()} is equal
   *   <li>{@link InstrumentDescriptor#getName()} is equal
   *   <li>{@link InstrumentDescriptor#getDescription()} is equal
   *   <li>{@link InstrumentDescriptor#getUnit()} is equal
   *   <li>{@link InstrumentDescriptor#getType()} is equal
   *   <li>{@link InstrumentDescriptor#getValueType()} is equal
   * </ul>
   */
  public boolean isCompatibleWith(MetricDescriptor other) {
    return getName().equals(other.getName())
        && getDescription().equals(other.getDescription())
        && getAggregationName().equals(other.getAggregationName())
        && getSourceInstrument().getName().equals(other.getSourceInstrument().getName())
        && getSourceInstrument()
            .getDescription()
            .equals(other.getSourceInstrument().getDescription())
        && getSourceInstrument().getUnit().equals(other.getSourceInstrument().getUnit())
        && getSourceInstrument().getType().equals(other.getSourceInstrument().getType())
        && getSourceInstrument().getValueType().equals(other.getSourceInstrument().getValueType());
  }
}
