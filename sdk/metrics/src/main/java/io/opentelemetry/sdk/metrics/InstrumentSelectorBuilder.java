/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

/** Builder for {@link InstrumentSelector}. */
public final class InstrumentSelectorBuilder {

  @Nullable private InstrumentType instrumentType;
  @Nullable private String instrumentName;
  @Nullable private String meterName;
  @Nullable private String meterVersion;
  @Nullable private String meterSchemaUrl;

  InstrumentSelectorBuilder() {}

  /** Sets a specifier for {@link InstrumentType}. */
  public InstrumentSelectorBuilder setType(InstrumentType instrumentType) {
    requireNonNull(instrumentType, "instrumentType");
    this.instrumentType = instrumentType;
    return this;
  }

  /**
   * Sets the exact instrument name that will be selected.
   *
   * <p>Instrument name may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  public InstrumentSelectorBuilder setName(String name) {
    requireNonNull(name, "name");
    this.instrumentName = name;
    return this;
  }

  /**
   * Sets a specifier for selecting instruments by the name of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterName(String meterName) {
    requireNonNull(meterName, "meterName");
    this.meterName = meterName;
    return this;
  }

  /**
   * Sets a specifier for selecting instruments by the version of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterVersion(String meterVersion) {
    requireNonNull(meterVersion, "meterVersion");
    this.meterVersion = meterVersion;
    return this;
  }

  /**
   * Sets a specifier for selecting instruments by the schema URL of their associated {@link
   * io.opentelemetry.api.metrics.Meter}.
   */
  public InstrumentSelectorBuilder setMeterSchemaUrl(String meterSchemaUrl) {
    requireNonNull(meterSchemaUrl, "meterSchemaUrl");
    this.meterSchemaUrl = meterSchemaUrl;
    return this;
  }

  /** Returns an InstrumentSelector instance with the content of this builder. */
  public InstrumentSelector build() {
    checkArgument(
        instrumentType != null
            || instrumentName != null
            || meterName != null
            || meterVersion != null
            || meterSchemaUrl != null,
        "Instrument selector must contain selection criteria");
    return InstrumentSelector.create(
        instrumentType, instrumentName, meterName, meterVersion, meterSchemaUrl);
  }
}
