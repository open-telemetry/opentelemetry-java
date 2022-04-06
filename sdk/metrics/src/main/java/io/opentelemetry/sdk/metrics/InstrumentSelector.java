/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
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
      @Nullable String instrumentName,
      @Nullable String meterName,
      @Nullable String meterVersion,
      @Nullable String meterSchemaUrl) {
    return new AutoValue_InstrumentSelector(
        instrumentType, instrumentName, meterName, meterVersion, meterSchemaUrl);
  }

  InstrumentSelector() {}

  /**
   * Returns selection criteria for {@link InstrumentType}. If null, select instruments with any
   * type.
   */
  @Nullable
  public abstract InstrumentType getInstrumentType();

  /**
   * Returns the selection criteria for instrument name. If null, select instruments with any name.
   *
   * <p>Instrument name may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  @Nullable
  public abstract String getInstrumentName();

  /**
   * Returns the selection criteria for meter name. If null, select instruments from meters with any
   * name.
   */
  @Nullable
  public abstract String getMeterName();

  /**
   * Returns the selection criteria for meter version. If null, select instruments from meters with
   * any version.
   */
  @Nullable
  public abstract String getMeterVersion();

  /**
   * Returns the selection criteria for meter schema url. If null, select instruments from meters
   * with any schema url.
   */
  @Nullable
  public abstract String getMeterSchemaUrl();
}
