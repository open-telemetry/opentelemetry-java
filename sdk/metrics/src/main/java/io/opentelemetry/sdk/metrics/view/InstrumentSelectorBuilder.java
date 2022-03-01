/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** Builder for {@link InstrumentSelector}. */
public final class InstrumentSelectorBuilder {

  @Nullable private InstrumentType instrumentType;
  private Predicate<String> instrumentNameFilter = StringPredicates.ALL;
  private MeterSelector meterSelector = MeterSelector.builder().build();

  /** Sets a specifier for {@link InstrumentType}. */
  public InstrumentSelectorBuilder setType(InstrumentType instrumentType) {
    requireNonNull(instrumentType, "instrumentType");
    this.instrumentType = instrumentType;
    return this;
  }

  /** Sets the exact instrument name that will be selected. */
  public InstrumentSelectorBuilder setName(String name) {
    requireNonNull(name, "name");
    return setName(StringPredicates.exact(name));
  }

  /**
   * Sets a {@link Predicate} where instrument names matching the {@link Predicate} will be
   * selected.
   */
  public InstrumentSelectorBuilder setName(Predicate<String> nameFilter) {
    requireNonNull(nameFilter, "nameFilter");
    this.instrumentNameFilter = nameFilter;
    return this;
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
