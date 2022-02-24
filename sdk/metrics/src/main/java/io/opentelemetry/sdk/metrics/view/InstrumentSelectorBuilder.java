/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/** Builder for {@link InstrumentSelector}. */
public final class InstrumentSelectorBuilder {

  @Nullable private InstrumentType instrumentType;
  private Predicate<String> instrumentNameFilter = StringPredicates.ALL;
  private MeterSelector meterSelector = MeterSelector.builder().build();

  /** Sets a specifier for {@link InstrumentType}. */
  public InstrumentSelectorBuilder setInstrumentType(InstrumentType instrumentType) {
    requireNonNull(instrumentType, "instrumentType");
    this.instrumentType = instrumentType;
    return this;
  }

  /**
   * Sets the {@link Pattern} for instrument names that will be selected.
   *
   * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
   * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
   * used.
   */
  public InstrumentSelectorBuilder setInstrumentNameFilter(Predicate<String> instrumentNameFilter) {
    requireNonNull(instrumentNameFilter, "instrumentNameFilter");
    this.instrumentNameFilter = instrumentNameFilter;
    return this;
  }

  /**
   * Sets the {@link Pattern} for instrument names that will be selected.
   *
   * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
   * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
   * used.
   */
  public InstrumentSelectorBuilder setInstrumentNamePattern(Pattern instrumentNamePattern) {
    requireNonNull(instrumentNamePattern, "instrumentNamePattern");
    return setInstrumentNameFilter(StringPredicates.regex(instrumentNamePattern));
  }

  /**
   * Sets the exact instrument name that will be selected.
   *
   * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
   * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
   * used.
   */
  public InstrumentSelectorBuilder setInstrumentName(String instrumentName) {
    requireNonNull(instrumentName, "instrumentName");
    return setInstrumentNameFilter(StringPredicates.exact(instrumentName));
  }

  /**
   * Sets a specifier for selecting Instruments by name.
   *
   * <p>Note: The last provided of {@link #setInstrumentNameFilter}, {@link
   * #setInstrumentNamePattern} {@link #setInstrumentNameRegex} and {@link #setInstrumentName} is
   * used.
   */
  public InstrumentSelectorBuilder setInstrumentNameRegex(String instrumentNameRegex) {
    requireNonNull(instrumentNameRegex, "instrumentNameRegex");
    return setInstrumentNamePattern(Pattern.compile(instrumentNameRegex));
  }

  /**
   * Sets the {@link MeterSelector} for which {@link io.opentelemetry.api.metrics.Meter}s will be
   * included.
   */
  public InstrumentSelectorBuilder setMeterSelector(MeterSelector meterSelector) {
    requireNonNull(meterSelector, "meterSelector");
    this.meterSelector = meterSelector;
    return this;
  }

  /** Returns an InstrumentSelector instance with the content of this builder. */
  public InstrumentSelector build() {
    return InstrumentSelector.create(instrumentType, instrumentNameFilter, meterSelector);
  }
}
