/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Instrument selection criteria for applying {@link View}s registered via {@link
 * SdkMeterProviderBuilder#registerView(InstrumentSelector, View)}.
 *
 * <p>Properties are ANDed together. For example, if {@link #getInstrumentName()} is
 * "http.server.duration" and {@link #getMeterName()} is "my.http.meter", then instruments are
 * selected where name is "http.server.duration" AND meter name is "my.http.meter".
 *
 * @since 1.14.0
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
      @Nullable String instrumentUnit,
      @Nullable String meterName,
      @Nullable String meterVersion,
      @Nullable String meterSchemaUrl) {
    return new AutoValue_InstrumentSelector(
        instrumentType, instrumentName, instrumentUnit, meterName, meterVersion, meterSchemaUrl);
  }

  InstrumentSelector() {}

  /** Returns the selected {@link InstrumentType}, or null if this selects all instrument types. */
  @Nullable
  public abstract InstrumentType getInstrumentType();

  /**
   * Returns the selected instrument name, or null if this selects all instrument names.
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
   * Returns the selected instrument unit, or null if this selects all instrument units.
   *
   * @since 1.24.0
   */
  @Nullable
  public abstract String getInstrumentUnit();

  /** Returns the selected meter name, or null if this selects instruments from all meter names. */
  @Nullable
  public abstract String getMeterName();

  /**
   * Returns the selected meter version, or null if this selects instruments from all meter
   * versions.
   */
  @Nullable
  public abstract String getMeterVersion();

  /**
   * Returns the selected meter schema url, or null if this selects instruments from all meter
   * schema urls.
   */
  @Nullable
  public abstract String getMeterSchemaUrl();

  @Override
  public final String toString() {
    StringJoiner joiner = new StringJoiner(", ", "InstrumentSelector{", "}");
    if (getInstrumentType() != null) {
      joiner.add("instrumentType=" + getInstrumentType());
    }
    if (getInstrumentName() != null) {
      joiner.add("instrumentName=" + getInstrumentName());
    }
    if (getInstrumentUnit() != null) {
      joiner.add("instrumentUnit=" + getInstrumentUnit());
    }
    if (getMeterName() != null) {
      joiner.add("meterName=" + getMeterName());
    }
    if (getMeterVersion() != null) {
      joiner.add("meterVersion=" + getMeterVersion());
    }
    if (getMeterSchemaUrl() != null) {
      joiner.add("meterSchemaUrl=" + getMeterSchemaUrl());
    }
    return joiner.toString();
  }
}
