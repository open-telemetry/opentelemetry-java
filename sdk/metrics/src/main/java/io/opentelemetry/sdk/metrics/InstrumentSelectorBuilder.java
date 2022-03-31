/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** Builder for {@link InstrumentSelector}. */
public final class InstrumentSelectorBuilder {

  @Nullable private InstrumentType instrumentType;
  private Predicate<String> instrumentNameFilter = StringPredicates.ALL;

  private Predicate<String> meterNameFilter = StringPredicates.ALL;
  private Predicate<String> meterVersionFilter = StringPredicates.ALL;
  private Predicate<String> meterSchemaUrlFilter = StringPredicates.ALL;

  InstrumentSelectorBuilder() {}

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
   * Sets a specifier for selecting instruments by the name of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterName(String meterName) {
    requireNonNull(meterName, "meterName");
    return setMeterName(StringPredicates.exact(meterName));
  }

  /**
   * Sets a {@link Predicate} for selecting instruments by the name of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterName(Predicate<String> meterNameFilter) {
    requireNonNull(meterNameFilter, "meterNameFilter");
    this.meterNameFilter = meterNameFilter;
    return this;
  }

  /**
   * Sets a specifier for selecting instruments by the version of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterVersion(String meterVersion) {
    requireNonNull(meterVersion, "meterVersion");
    return setMeterVersion(StringPredicates.exact(meterVersion));
  }

  /**
   * Sets a {@link Predicate} for selecting instruments by the name of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterVersion(Predicate<String> meterVersionFilter) {
    requireNonNull(meterVersionFilter, "meterVersionFilter");
    this.meterVersionFilter = meterVersionFilter;
    return this;
  }

  /**
   * Sets a specifier for selecting instruments by the schema URL of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterSchemaUrl(String meterSchemaUrl) {
    requireNonNull(meterSchemaUrl, "meterSchemaUrl");
    return setMeterSchemaUrl(StringPredicates.exact(meterSchemaUrl));
  }

  /**
   * Sets a {@link Predicate} for selecting instruments by the name of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterSchemaUrl(Predicate<String> meterSchemaUrlFilter) {
    requireNonNull(meterSchemaUrlFilter, "meterSchemaUrlFilter");
    this.meterSchemaUrlFilter = meterSchemaUrlFilter;
    return this;
  }

  /** Returns an InstrumentSelector instance with the content of this builder. */
  public InstrumentSelector build() {
    checkArgument(
        instrumentType != null
            || instrumentNameFilter != StringPredicates.ALL
            || meterNameFilter != StringPredicates.ALL
            || meterVersionFilter != StringPredicates.ALL
            || meterSchemaUrlFilter != StringPredicates.ALL,
        "Instrument selector must contain selection criteria");
    return InstrumentSelector.create(
        instrumentType,
        instrumentNameFilter,
        meterNameFilter,
        meterVersionFilter,
        meterSchemaUrlFilter);
  }
}
