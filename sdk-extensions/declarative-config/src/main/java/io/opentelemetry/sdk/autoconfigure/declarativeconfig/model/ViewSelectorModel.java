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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ViewSelectorModel {

  /**
   * Configure instrument name selection criteria. If omitted or null, all instrument names match.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("instrument_name")
  @JsonPropertyDescription(
      "Configure instrument name selection criteria.\nIf omitted or null, all instrument names match.\n")
  private String instrumentName;

  /** (Can be null) */
  @Nullable
  @JsonProperty("instrument_type")
  private ViewSelectorModel.InstrumentType instrumentType;

  /**
   * Configure the instrument unit selection criteria. If omitted or null, all instrument units
   * match.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("unit")
  @JsonPropertyDescription(
      "Configure the instrument unit selection criteria.\nIf omitted or null, all instrument units match.\n")
  private String unit;

  /**
   * Configure meter name selection criteria. If omitted or null, all meter names match.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("meter_name")
  @JsonPropertyDescription(
      "Configure meter name selection criteria.\nIf omitted or null, all meter names match.\n")
  private String meterName;

  /**
   * Configure meter version selection criteria. If omitted or null, all meter versions match.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("meter_version")
  @JsonPropertyDescription(
      "Configure meter version selection criteria.\nIf omitted or null, all meter versions match.\n")
  private String meterVersion;

  /**
   * Configure meter schema url selection criteria. If omitted or null, all meter schema URLs match.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("meter_schema_url")
  @JsonPropertyDescription(
      "Configure meter schema url selection criteria.\nIf omitted or null, all meter schema URLs match.\n")
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
    StringBuilder sb = new StringBuilder();
    sb.append(ViewSelectorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("instrumentName");
    sb.append('=');
    sb.append(((this.instrumentName == null) ? "<null>" : this.instrumentName));
    sb.append(',');
    sb.append("instrumentType");
    sb.append('=');
    sb.append(((this.instrumentType == null) ? "<null>" : this.instrumentType));
    sb.append(',');
    sb.append("unit");
    sb.append('=');
    sb.append(((this.unit == null) ? "<null>" : this.unit));
    sb.append(',');
    sb.append("meterName");
    sb.append('=');
    sb.append(((this.meterName == null) ? "<null>" : this.meterName));
    sb.append(',');
    sb.append("meterVersion");
    sb.append('=');
    sb.append(((this.meterVersion == null) ? "<null>" : this.meterVersion));
    sb.append(',');
    sb.append("meterSchemaUrl");
    sb.append('=');
    sb.append(((this.meterSchemaUrl == null) ? "<null>" : this.meterSchemaUrl));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.instrumentName == null) ? 0 : this.instrumentName.hashCode()));
    result = ((result * 31) + ((this.instrumentType == null) ? 0 : this.instrumentType.hashCode()));
    result = ((result * 31) + ((this.unit == null) ? 0 : this.unit.hashCode()));
    result = ((result * 31) + ((this.meterSchemaUrl == null) ? 0 : this.meterSchemaUrl.hashCode()));
    result = ((result * 31) + ((this.meterName == null) ? 0 : this.meterName.hashCode()));
    result = ((result * 31) + ((this.meterVersion == null) ? 0 : this.meterVersion.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ViewSelectorModel) == false) {
      return false;
    }
    ViewSelectorModel rhs = ((ViewSelectorModel) other);
    return (((((((this.instrumentName == rhs.instrumentName)
                            || ((this.instrumentName != null)
                                && this.instrumentName.equals(rhs.instrumentName)))
                        && ((this.instrumentType == rhs.instrumentType)
                            || ((this.instrumentType != null)
                                && this.instrumentType.equals(rhs.instrumentType))))
                    && ((this.unit == rhs.unit)
                        || ((this.unit != null) && this.unit.equals(rhs.unit))))
                && ((this.meterSchemaUrl == rhs.meterSchemaUrl)
                    || ((this.meterSchemaUrl != null)
                        && this.meterSchemaUrl.equals(rhs.meterSchemaUrl))))
            && ((this.meterName == rhs.meterName)
                || ((this.meterName != null) && this.meterName.equals(rhs.meterName))))
        && ((this.meterVersion == rhs.meterVersion)
            || ((this.meterVersion != null) && this.meterVersion.equals(rhs.meterVersion))));
  }

  @Generated("jsonschema2pojo")
  @SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
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
