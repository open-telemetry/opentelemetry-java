/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "instrument_name",
  "instrument_type",
  "unit",
  "meter_name",
  "meter_version",
  "meter_schema_url"
})
@Generated("jsonschema2pojo")
public class ViewSelectorModel {

  /**
   * Configure instrument name selection criteria. If omitted or null, all instrument names match.
   */
  @JsonProperty("instrument_name")
  @JsonPropertyDescription(
      "Configure instrument name selection criteria.\nIf omitted or null, all instrument names match.\n")
  @Nullable
  private String instrumentName;

  @JsonProperty("instrument_type")
  @Nullable
  private ViewSelectorModel.InstrumentType instrumentType;

  /**
   * Configure the instrument unit selection criteria. If omitted or null, all instrument units
   * match.
   */
  @JsonProperty("unit")
  @JsonPropertyDescription(
      "Configure the instrument unit selection criteria.\nIf omitted or null, all instrument units match.\n")
  @Nullable
  private String unit;

  /** Configure meter name selection criteria. If omitted or null, all meter names match. */
  @JsonProperty("meter_name")
  @JsonPropertyDescription(
      "Configure meter name selection criteria.\nIf omitted or null, all meter names match.\n")
  @Nullable
  private String meterName;

  /** Configure meter version selection criteria. If omitted or null, all meter versions match. */
  @JsonProperty("meter_version")
  @JsonPropertyDescription(
      "Configure meter version selection criteria.\nIf omitted or null, all meter versions match.\n")
  @Nullable
  private String meterVersion;

  /**
   * Configure meter schema url selection criteria. If omitted or null, all meter schema URLs match.
   */
  @JsonProperty("meter_schema_url")
  @JsonPropertyDescription(
      "Configure meter schema url selection criteria.\nIf omitted or null, all meter schema URLs match.\n")
  @Nullable
  private String meterSchemaUrl;

  /**
   * Configure instrument name selection criteria. If omitted or null, all instrument names match.
   */
  @JsonProperty("instrument_name")
  @Nullable
  public String getInstrumentName() {
    return instrumentName;
  }

  public ViewSelectorModel withInstrumentName(String instrumentName) {
    this.instrumentName = instrumentName;
    return this;
  }

  @JsonProperty("instrument_type")
  @Nullable
  public ViewSelectorModel.InstrumentType getInstrumentType() {
    return instrumentType;
  }

  public ViewSelectorModel withInstrumentType(ViewSelectorModel.InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
    return this;
  }

  /**
   * Configure the instrument unit selection criteria. If omitted or null, all instrument units
   * match.
   */
  @JsonProperty("unit")
  @Nullable
  public String getUnit() {
    return unit;
  }

  public ViewSelectorModel withUnit(String unit) {
    this.unit = unit;
    return this;
  }

  /** Configure meter name selection criteria. If omitted or null, all meter names match. */
  @JsonProperty("meter_name")
  @Nullable
  public String getMeterName() {
    return meterName;
  }

  public ViewSelectorModel withMeterName(String meterName) {
    this.meterName = meterName;
    return this;
  }

  /** Configure meter version selection criteria. If omitted or null, all meter versions match. */
  @JsonProperty("meter_version")
  @Nullable
  public String getMeterVersion() {
    return meterVersion;
  }

  public ViewSelectorModel withMeterVersion(String meterVersion) {
    this.meterVersion = meterVersion;
    return this;
  }

  /**
   * Configure meter schema url selection criteria. If omitted or null, all meter schema URLs match.
   */
  @JsonProperty("meter_schema_url")
  @Nullable
  public String getMeterSchemaUrl() {
    return meterSchemaUrl;
  }

  public ViewSelectorModel withMeterSchemaUrl(String meterSchemaUrl) {
    this.meterSchemaUrl = meterSchemaUrl;
    return this;
  }

  @Override
  public String toString() {
    return "ViewSelectorModel{"
        + "instrumentName="
        + instrumentName
        + ", instrumentType="
        + instrumentType
        + ", unit="
        + unit
        + ", meterName="
        + meterName
        + ", meterVersion="
        + meterVersion
        + ", meterSchemaUrl="
        + meterSchemaUrl
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.instrumentName == null) ? 0 : this.instrumentName.hashCode();
    h *= 1000003;
    h ^= (this.instrumentType == null) ? 0 : this.instrumentType.hashCode();
    h *= 1000003;
    h ^= (this.unit == null) ? 0 : this.unit.hashCode();
    h *= 1000003;
    h ^= (this.meterName == null) ? 0 : this.meterName.hashCode();
    h *= 1000003;
    h ^= (this.meterVersion == null) ? 0 : this.meterVersion.hashCode();
    h *= 1000003;
    h ^= (this.meterSchemaUrl == null) ? 0 : this.meterSchemaUrl.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ViewSelectorModel) {
      ViewSelectorModel that = (ViewSelectorModel) o;
      return (this.instrumentName == null
              ? that.instrumentName == null
              : this.instrumentName.equals(that.instrumentName))
          && (this.instrumentType == null
              ? that.instrumentType == null
              : this.instrumentType.equals(that.instrumentType))
          && (this.unit == null ? that.unit == null : this.unit.equals(that.unit))
          && (this.meterName == null
              ? that.meterName == null
              : this.meterName.equals(that.meterName))
          && (this.meterVersion == null
              ? that.meterVersion == null
              : this.meterVersion.equals(that.meterVersion))
          && (this.meterSchemaUrl == null
              ? that.meterSchemaUrl == null
              : this.meterSchemaUrl.equals(that.meterSchemaUrl));
    }
    return false;
  }

  @Generated("jsonschema2pojo")
  public enum InstrumentType {
    COUNTER("counter"),
    GAUGE("gauge"),
    HISTOGRAM("histogram"),
    OBSERVABLE_COUNTER("observable_counter"),
    OBSERVABLE_GAUGE("observable_gauge"),
    OBSERVABLE_UP_DOWN_COUNTER("observable_up_down_counter"),
    UP_DOWN_COUNTER("up_down_counter");
    private final String value;
    private static final Map<String, ViewSelectorModel.InstrumentType> CONSTANTS =
        new HashMap<String, ViewSelectorModel.InstrumentType>();

    static {
      for (ViewSelectorModel.InstrumentType c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    InstrumentType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static ViewSelectorModel.InstrumentType fromValue(String value) {
      ViewSelectorModel.InstrumentType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
