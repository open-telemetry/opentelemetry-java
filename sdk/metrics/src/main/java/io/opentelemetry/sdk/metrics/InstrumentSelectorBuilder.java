/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

/**
 * Builder for {@link InstrumentSelector}.
 *
 * @since 1.14.0
 */
public final class InstrumentSelectorBuilder {

  @Nullable private InstrumentType instrumentType;
  @Nullable private String instrumentName;
  @Nullable private String instrumentUnit;
  @Nullable private String meterName;
  @Nullable private String meterVersion;
  @Nullable private String meterSchemaUrl;

  InstrumentSelectorBuilder() {}

  /** Select instruments with the given {@code instrumentType}. */
  public InstrumentSelectorBuilder setType(InstrumentType instrumentType) {
    requireNonNull(instrumentType, "instrumentType");
    this.instrumentType = instrumentType;
    return this;
  }

  /**
   * Select instruments with the given {@code name}.
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
   * Select instruments with the given {@code unit}.
   *
   * @since 1.24.0
   */
  public InstrumentSelectorBuilder setUnit(String unit) {
    requireNonNull(unit, "unit");
    this.instrumentUnit = unit;
    return this;
  }

  /** Select instruments associated with the given {@code meterName}. */
  public InstrumentSelectorBuilder setMeterName(String meterName) {
    requireNonNull(meterName, "meterName");
    this.meterName = meterName;
    return this;
  }

  /** Select instruments associated with the given {@code meterVersion}. */
  public InstrumentSelectorBuilder setMeterVersion(String meterVersion) {
    requireNonNull(meterVersion, "meterVersion");
    this.meterVersion = meterVersion;
    return this;
  }

  /** Select instruments associated with the given {@code meterSchemaUrl}. */
  public InstrumentSelectorBuilder setMeterSchemaUrl(String meterSchemaUrl) {
    requireNonNull(meterSchemaUrl, "meterSchemaUrl");
    this.meterSchemaUrl = meterSchemaUrl;
    return this;
  }

  /** Returns an {@link InstrumentSelector} with the configuration of this builder. */
  public InstrumentSelector build() {
    checkArgument(
        instrumentType != null
            || instrumentName != null
            || instrumentUnit != null
            || meterName != null
            || meterVersion != null
            || meterSchemaUrl != null,
        "Instrument selector must contain selection criteria");
    return InstrumentSelector.create(
        instrumentType, instrumentName, instrumentUnit, meterName, meterVersion, meterSchemaUrl);
  }
}
