/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.InstrumentType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Provides means for selecting one or more instruments. Used for configuring aggregations for the
 * specified instruments.
 */
@AutoValue
@Immutable
public abstract class InstrumentSelector {

  /** Returns a new {@link InstrumentSelectorBuilder} for {@link InstrumentSelector}. */
  public static InstrumentSelectorBuilder builder() {
    return new InstrumentSelectorBuilder();
  }

  static InstrumentSelector create(
      @Nullable InstrumentType instrumentType,
      Predicate<String> instrumentNameFilter,
      Predicate<String> meterNameFilter,
      Predicate<String> meterVersionFilter,
      Predicate<String> meterSchemaUrlFilter) {
    return new AutoValue_InstrumentSelector(
        instrumentType,
        instrumentNameFilter,
        meterNameFilter,
        meterVersionFilter,
        meterSchemaUrlFilter);
  }

  InstrumentSelector() {}

  /**
   * Returns {@link InstrumentType} that should be selected. If null, then this specifier will not
   * be used.
   */
  @Nullable
  public abstract InstrumentType getInstrumentType();

  /**
   * Returns the {@link Predicate} for filtering instruments by name. Matches everything by default.
   */
  public abstract Predicate<String> getInstrumentNameFilter();

  /**
   * Returns the {@link Predicate} for filtering instruments by the name of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public abstract Predicate<String> getMeterNameFilter();

  /**
   * Returns the {@link Predicate} for filtering instruments by the version of their associated
   * {@link io.opentelemetry.api.metrics.Meter}.
   */
  public abstract Predicate<String> getMeterVersionFilter();

  /**
   * Returns the {@link Predicate} for filtering instruments by the schema URL of their associated
   * {@link io.opentelemetry.api.metrics.Meter}.
   */
  public abstract Predicate<String> getMeterSchemaUrlFilter();

  /**
   * Returns the selections criteria for {@link io.opentelemetry.api.metrics.Meter}s.
   *
   * @deprecated Use {@link #getMeterNameFilter()} and similar.
   */
  @Deprecated
  public final MeterSelector getMeterSelector() {
    return MeterSelector.create(
        getMeterNameFilter(), getMeterVersionFilter(), getMeterSchemaUrlFilter());
  }
}
