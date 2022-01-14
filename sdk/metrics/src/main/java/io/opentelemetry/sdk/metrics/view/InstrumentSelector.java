/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Provides means for selecting one or more instruments. Used for configuring aggregations for the
 * specified instruments.
 */
@AutoValue
@Immutable
public abstract class InstrumentSelector {

  /**
   * Returns a new {@link Builder} for {@link InstrumentSelector}.
   *
   * @return a new {@link Builder} for {@link InstrumentSelector}.
   */
  public static Builder builder() {
    return new AutoValue_InstrumentSelector.Builder()
        .setInstrumentNameFilter(StringPredicates.ALL)
        .setMeterSelector(MeterSelector.builder().build());
  }

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

  /** Returns the selections criteria for {@link io.opentelemetry.api.metrics.Meter}s. */
  public abstract MeterSelector getMeterSelector();

  /** Builder for {@link InstrumentSelector} instances. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets a specifier for {@link InstrumentType}. */
    public abstract Builder setInstrumentType(InstrumentType instrumentType);

    /**
     * Sets the {@link Pattern} for instrument names that will be selected.
     *
     * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
     * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
     * used.
     */
    public abstract Builder setInstrumentNameFilter(Predicate<String> instrumentNameFilter);

    /**
     * Sets the {@link Pattern} for instrument names that will be selected.
     *
     * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
     * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
     * used.
     */
    public final Builder setInstrumentNamePattern(Pattern instrumentNamePattern) {
      return setInstrumentNameFilter(StringPredicates.regex(instrumentNamePattern));
    }

    /**
     * Sets the exact instrument name that will be selected.
     *
     * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
     * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
     * used.
     */
    public final Builder setInstrumentName(String instrumentName) {
      return setInstrumentNameFilter(StringPredicates.exact(instrumentName));
    }

    /**
     * Sets a specifier for selecting Instruments by name.
     *
     * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
     * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
     * used.
     */
    public final Builder setInstrumentNameRegex(String regex) {
      return setInstrumentNamePattern(Pattern.compile(Objects.requireNonNull(regex, "regex")));
    }

    /**
     * Sets the {@link MeterSelector} for which {@link io.opentelemetry.api.metrics.Meter}s will be
     * included.
     */
    public abstract Builder setMeterSelector(MeterSelector meterSelector);

    /** Returns an InstrumentSelector instance with the content of this builder. */
    public abstract InstrumentSelector build();
  }
}
