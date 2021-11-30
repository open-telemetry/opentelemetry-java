/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.Objects;
import java.util.Optional;
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
   * Constructs a metric descriptor with no source instrument/view.
   *
   * <p>Used for testing + empty-storage only.
   */
  public static MetricDescriptor create(String name, String description, String unit) {
    return new AutoValue_MetricDescriptor(
        name,
        description,
        unit,
        Optional.empty(),
        InstrumentDescriptor.create(
            name, description, unit, InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.DOUBLE));
  }

  /** Constructs a metric descriptor for a given View + instrument. */
  public static MetricDescriptor create(View view, InstrumentDescriptor instrument) {
    final String name = (view.getName() == null) ? instrument.getName() : view.getName();
    final String description =
        (view.getDescription() == null) ? instrument.getDescription() : view.getDescription();
    return new AutoValue_MetricDescriptor(
        name, description, instrument.getUnit(), Optional.of(view), instrument);
  }

  public abstract String getName();

  public abstract String getDescription();

  public abstract String getUnit();

  /** The view that lead to the creation of this metric, if applicable. */
  public abstract Optional<View> getSourceView();
  /** The instrument which lead to the creation of this metric. */
  public abstract InstrumentDescriptor getSourceInstrument();

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
   *   <li>{@link #getUnit()} is equal
   *   <li>{@link InstrumentDescriptor#getType()} is equal
   *   <li>{@link InstrumentDescriptor#getValueType()} is equal
   * </ul>
   */
  public boolean isCompatibleWith(MetricDescriptor other) {
    return Objects.equals(getName(), other.getName())
        && Objects.equals(getDescription(), other.getDescription())
        && Objects.equals(getUnit(), other.getUnit())
        && Objects.equals(getSourceInstrument().getType(), other.getSourceInstrument().getType())
        && Objects.equals(
            getSourceInstrument().getValueType(), other.getSourceInstrument().getValueType());
  }
}
