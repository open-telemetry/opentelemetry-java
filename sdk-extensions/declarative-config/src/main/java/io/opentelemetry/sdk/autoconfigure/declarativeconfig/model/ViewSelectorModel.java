/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel.INSTRUMENT_NAME;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel.INSTRUMENT_TYPE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel.METER_NAME;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel.METER_SCHEMA_URL;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel.METER_VERSION;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel.UNIT;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  INSTRUMENT_NAME,
  INSTRUMENT_TYPE,
  UNIT,
  METER_NAME,
  METER_VERSION,
  METER_SCHEMA_URL
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ViewSelectorModel {

  static final String INSTRUMENT_NAME = "instrument_name";
  static final String INSTRUMENT_TYPE = "instrument_type";
  static final String UNIT = "unit";
  static final String METER_NAME = "meter_name";
  static final String METER_VERSION = "meter_version";
  static final String METER_SCHEMA_URL = "meter_schema_url";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(INSTRUMENT_NAME, String.class);
    STABLE_PROPERTIES.put(INSTRUMENT_TYPE, InstrumentTypeModel.class);
    STABLE_PROPERTIES.put(UNIT, String.class);
    STABLE_PROPERTIES.put(METER_NAME, String.class);
    STABLE_PROPERTIES.put(METER_VERSION, String.class);
    STABLE_PROPERTIES.put(METER_SCHEMA_URL, String.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private String instrumentName;
  @Nullable private InstrumentTypeModel instrumentType;
  @Nullable private String unit;
  @Nullable private String meterName;
  @Nullable private String meterVersion;
  @Nullable private String meterSchemaUrl;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure instrument name selection criteria.
   *
   * <p>If omitted or null, all instrument names match.
   */
  @JsonProperty(INSTRUMENT_NAME)
  @Nullable
  public String getInstrumentName() {
    if (instrumentName == null) {
      return ExtensionPropertyUtil.getGraduated(INSTRUMENT_NAME, extensionProperties, String.class);
    }
    return instrumentName;
  }

  @JsonProperty(INSTRUMENT_NAME)
  public ViewSelectorModel setInstrumentName(String instrumentName) {
    this.instrumentName = instrumentName;
    return this;
  }

  /**
   * Configure instrument type selection criteria.
   *
   * <p>Values include:
   *
   * <p>* counter: Synchronous counter instruments.
   *
   * <p>* gauge: Synchronous gauge instruments.
   *
   * <p>* histogram: Synchronous histogram instruments.
   *
   * <p>* observable_counter: Asynchronous counter instruments.
   *
   * <p>* observable_gauge: Asynchronous gauge instruments.
   *
   * <p>* observable_up_down_counter: Asynchronous up down counter instruments.
   *
   * <p>* up_down_counter: Synchronous up down counter instruments.
   *
   * <p>If omitted, all instrument types match.
   */
  @JsonProperty(INSTRUMENT_TYPE)
  @Nullable
  public InstrumentTypeModel getInstrumentType() {
    if (instrumentType == null) {
      return ExtensionPropertyUtil.getGraduated(
          INSTRUMENT_TYPE, extensionProperties, InstrumentTypeModel.class);
    }
    return instrumentType;
  }

  @JsonProperty(INSTRUMENT_TYPE)
  public ViewSelectorModel setInstrumentType(InstrumentTypeModel instrumentType) {
    this.instrumentType = instrumentType;
    return this;
  }

  /**
   * Configure the instrument unit selection criteria.
   *
   * <p>If omitted or null, all instrument units match.
   */
  @JsonProperty(UNIT)
  @Nullable
  public String getUnit() {
    if (unit == null) {
      return ExtensionPropertyUtil.getGraduated(UNIT, extensionProperties, String.class);
    }
    return unit;
  }

  @JsonProperty(UNIT)
  public ViewSelectorModel setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  /**
   * Configure meter name selection criteria.
   *
   * <p>If omitted or null, all meter names match.
   */
  @JsonProperty(METER_NAME)
  @Nullable
  public String getMeterName() {
    if (meterName == null) {
      return ExtensionPropertyUtil.getGraduated(METER_NAME, extensionProperties, String.class);
    }
    return meterName;
  }

  @JsonProperty(METER_NAME)
  public ViewSelectorModel setMeterName(String meterName) {
    this.meterName = meterName;
    return this;
  }

  /**
   * Configure meter version selection criteria.
   *
   * <p>If omitted or null, all meter versions match.
   */
  @JsonProperty(METER_VERSION)
  @Nullable
  public String getMeterVersion() {
    if (meterVersion == null) {
      return ExtensionPropertyUtil.getGraduated(METER_VERSION, extensionProperties, String.class);
    }
    return meterVersion;
  }

  @JsonProperty(METER_VERSION)
  public ViewSelectorModel setMeterVersion(String meterVersion) {
    this.meterVersion = meterVersion;
    return this;
  }

  /**
   * Configure meter schema url selection criteria.
   *
   * <p>If omitted or null, all meter schema URLs match.
   */
  @JsonProperty(METER_SCHEMA_URL)
  @Nullable
  public String getMeterSchemaUrl() {
    if (meterSchemaUrl == null) {
      return ExtensionPropertyUtil.getGraduated(
          METER_SCHEMA_URL, extensionProperties, String.class);
    }
    return meterSchemaUrl;
  }

  @JsonProperty(METER_SCHEMA_URL)
  public ViewSelectorModel setMeterSchemaUrl(String meterSchemaUrl) {
    this.meterSchemaUrl = meterSchemaUrl;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public ViewSelectorModel setExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
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
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.getInstrumentName() == null) ? 0 : this.getInstrumentName().hashCode();
    h *= 1000003;
    h ^= (this.getInstrumentType() == null) ? 0 : this.getInstrumentType().hashCode();
    h *= 1000003;
    h ^= (this.getUnit() == null) ? 0 : this.getUnit().hashCode();
    h *= 1000003;
    h ^= (this.getMeterName() == null) ? 0 : this.getMeterName().hashCode();
    h *= 1000003;
    h ^= (this.getMeterVersion() == null) ? 0 : this.getMeterVersion().hashCode();
    h *= 1000003;
    h ^= (this.getMeterSchemaUrl() == null) ? 0 : this.getMeterSchemaUrl().hashCode();
    h *= 1000003;
    h ^= (this.getExtensionProperties() == null) ? 0 : this.getExtensionProperties().hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ViewSelectorModel) {
      ViewSelectorModel that = (ViewSelectorModel) o;
      return (this.getInstrumentName() == null
              ? that.getInstrumentName() == null
              : this.getInstrumentName().equals(that.getInstrumentName()))
          && (this.getInstrumentType() == null
              ? that.getInstrumentType() == null
              : this.getInstrumentType().equals(that.getInstrumentType()))
          && (this.getUnit() == null
              ? that.getUnit() == null
              : this.getUnit().equals(that.getUnit()))
          && (this.getMeterName() == null
              ? that.getMeterName() == null
              : this.getMeterName().equals(that.getMeterName()))
          && (this.getMeterVersion() == null
              ? that.getMeterVersion() == null
              : this.getMeterVersion().equals(that.getMeterVersion()))
          && (this.getMeterSchemaUrl() == null
              ? that.getMeterSchemaUrl() == null
              : this.getMeterSchemaUrl().equals(that.getMeterSchemaUrl()))
          && (this.getExtensionProperties() == null
              ? that.getExtensionProperties() == null
              : this.getExtensionProperties().equals(that.getExtensionProperties()));
    }
    return false;
  }
}
