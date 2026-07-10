/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalInstrumentationModel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

/**
 * OpenTelemetryConfiguration
 *
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "file_format",
  "disabled",
  "log_level",
  "attribute_limits",
  "logger_provider",
  "meter_provider",
  "propagator",
  "tracer_provider",
  "resource",
  "instrumentation/development",
  "distribution"
})
@Generated("jsonschema2pojo")
public class OpenTelemetryConfigurationModel {

  @Nullable private String fileFormat;
  @Nullable private Boolean disabled;
  @Nullable private OpenTelemetryConfigurationModel.SeverityNumber logLevel;
  @Nullable private AttributeLimitsModel attributeLimits;
  @Nullable private LoggerProviderModel loggerProvider;
  @Nullable private MeterProviderModel meterProvider;
  @Nullable private PropagatorModel propagator;
  @Nullable private TracerProviderModel tracerProvider;
  @Nullable private ResourceModel resource;
  @Nullable private ExperimentalInstrumentationModel instrumentationDevelopment;
  @Nullable private DistributionModel distribution;
  private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

  /**
   * The file format version.
   *
   * <p>Represented as a string including the semver major, minor version numbers (and optionally
   * the meta tag). For example: "0.4", "1.0-rc.2", "1.0" (after stable release).
   *
   * <p>See https://github.com/open-telemetry/opentelemetry-configuration/blob/main/VERSIONING.md
   * for more details.
   *
   * <p>The yaml format is documented at
   * https://github.com/open-telemetry/opentelemetry-configuration/tree/main/schema
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("file_format")
  @Nullable
  public String getFileFormat() {
    return fileFormat;
  }

  @JsonProperty("file_format")
  public OpenTelemetryConfigurationModel withFileFormat(String fileFormat) {
    this.fileFormat = fileFormat;
    return this;
  }

  /**
   * Configure if the SDK is disabled or not.
   *
   * <p>If omitted or null, false is used.
   */
  @JsonProperty("disabled")
  @Nullable
  public Boolean getDisabled() {
    return disabled;
  }

  @JsonProperty("disabled")
  public OpenTelemetryConfigurationModel withDisabled(Boolean disabled) {
    this.disabled = disabled;
    return this;
  }

  /**
   * Configure the log level of the internal logger used by the SDK.
   *
   * <p>Values include:
   *
   * <p>* debug: debug, severity number 5.
   *
   * <p>* debug2: debug2, severity number 6.
   *
   * <p>* debug3: debug3, severity number 7.
   *
   * <p>* debug4: debug4, severity number 8.
   *
   * <p>* error: error, severity number 17.
   *
   * <p>* error2: error2, severity number 18.
   *
   * <p>* error3: error3, severity number 19.
   *
   * <p>* error4: error4, severity number 20.
   *
   * <p>* fatal: fatal, severity number 21.
   *
   * <p>* fatal2: fatal2, severity number 22.
   *
   * <p>* fatal3: fatal3, severity number 23.
   *
   * <p>* fatal4: fatal4, severity number 24.
   *
   * <p>* info: info, severity number 9.
   *
   * <p>* info2: info2, severity number 10.
   *
   * <p>* info3: info3, severity number 11.
   *
   * <p>* info4: info4, severity number 12.
   *
   * <p>* trace: trace, severity number 1.
   *
   * <p>* trace2: trace2, severity number 2.
   *
   * <p>* trace3: trace3, severity number 3.
   *
   * <p>* trace4: trace4, severity number 4.
   *
   * <p>* warn: warn, severity number 13.
   *
   * <p>* warn2: warn2, severity number 14.
   *
   * <p>* warn3: warn3, severity number 15.
   *
   * <p>* warn4: warn4, severity number 16.
   *
   * <p>If omitted, INFO is used.
   */
  @JsonProperty("log_level")
  @Nullable
  public OpenTelemetryConfigurationModel.SeverityNumber getLogLevel() {
    return logLevel;
  }

  @JsonProperty("log_level")
  public OpenTelemetryConfigurationModel withLogLevel(
      OpenTelemetryConfigurationModel.SeverityNumber logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  /**
   * Configure general attribute limits. See also tracer_provider.limits, logger_provider.limits.
   *
   * <p>If omitted, default values as described in AttributeLimits are used.
   */
  @JsonProperty("attribute_limits")
  @Nullable
  public AttributeLimitsModel getAttributeLimits() {
    return attributeLimits;
  }

  @JsonProperty("attribute_limits")
  public OpenTelemetryConfigurationModel withAttributeLimits(AttributeLimitsModel attributeLimits) {
    this.attributeLimits = attributeLimits;
    return this;
  }

  /**
   * Configure logger provider.
   *
   * <p>If omitted, a noop logger provider is used.
   */
  @JsonProperty("logger_provider")
  @Nullable
  public LoggerProviderModel getLoggerProvider() {
    return loggerProvider;
  }

  @JsonProperty("logger_provider")
  public OpenTelemetryConfigurationModel withLoggerProvider(LoggerProviderModel loggerProvider) {
    this.loggerProvider = loggerProvider;
    return this;
  }

  /**
   * Configure meter provider.
   *
   * <p>If omitted, a noop meter provider is used.
   */
  @JsonProperty("meter_provider")
  @Nullable
  public MeterProviderModel getMeterProvider() {
    return meterProvider;
  }

  @JsonProperty("meter_provider")
  public OpenTelemetryConfigurationModel withMeterProvider(MeterProviderModel meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  /**
   * Configure text map context propagators.
   *
   * <p>If omitted, a noop propagator is used.
   */
  @JsonProperty("propagator")
  @Nullable
  public PropagatorModel getPropagator() {
    return propagator;
  }

  @JsonProperty("propagator")
  public OpenTelemetryConfigurationModel withPropagator(PropagatorModel propagator) {
    this.propagator = propagator;
    return this;
  }

  /**
   * Configure tracer provider.
   *
   * <p>If omitted, a noop tracer provider is used.
   */
  @JsonProperty("tracer_provider")
  @Nullable
  public TracerProviderModel getTracerProvider() {
    return tracerProvider;
  }

  @JsonProperty("tracer_provider")
  public OpenTelemetryConfigurationModel withTracerProvider(TracerProviderModel tracerProvider) {
    this.tracerProvider = tracerProvider;
    return this;
  }

  /**
   * Configure resource for all signals.
   *
   * <p>If omitted, the default resource is used.
   */
  @JsonProperty("resource")
  @Nullable
  public ResourceModel getResource() {
    return resource;
  }

  @JsonProperty("resource")
  public OpenTelemetryConfigurationModel withResource(ResourceModel resource) {
    this.resource = resource;
    return this;
  }

  /**
   * Configure instrumentation.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("instrumentation/development")
  @Nullable
  public ExperimentalInstrumentationModel getInstrumentationDevelopment() {
    return instrumentationDevelopment;
  }

  @JsonProperty("instrumentation/development")
  public OpenTelemetryConfigurationModel withInstrumentationDevelopment(
      ExperimentalInstrumentationModel instrumentationDevelopment) {
    this.instrumentationDevelopment = instrumentationDevelopment;
    return this;
  }

  /**
   * Defines configuration parameters specific to a particular OpenTelemetry distribution or vendor.
   *
   * <p>This section provides a standardized location for distribution-specific settings
   *
   * <p>that are not part of the OpenTelemetry configuration model.
   *
   * <p>It allows vendors to expose their own extensions and general configuration options.
   *
   * <p>If omitted, distribution defaults are used.
   */
  @JsonProperty("distribution")
  @Nullable
  public DistributionModel getDistribution() {
    return distribution;
  }

  @JsonProperty("distribution")
  public OpenTelemetryConfigurationModel withDistribution(DistributionModel distribution) {
    this.distribution = distribution;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public OpenTelemetryConfigurationModel withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "OpenTelemetryConfigurationModel{"
        + "fileFormat="
        + fileFormat
        + ", disabled="
        + disabled
        + ", logLevel="
        + logLevel
        + ", attributeLimits="
        + attributeLimits
        + ", loggerProvider="
        + loggerProvider
        + ", meterProvider="
        + meterProvider
        + ", propagator="
        + propagator
        + ", tracerProvider="
        + tracerProvider
        + ", resource="
        + resource
        + ", instrumentationDevelopment="
        + instrumentationDevelopment
        + ", distribution="
        + distribution
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.fileFormat == null) ? 0 : this.fileFormat.hashCode();
    h *= 1000003;
    h ^= (this.disabled == null) ? 0 : this.disabled.hashCode();
    h *= 1000003;
    h ^= (this.logLevel == null) ? 0 : this.logLevel.hashCode();
    h *= 1000003;
    h ^= (this.attributeLimits == null) ? 0 : this.attributeLimits.hashCode();
    h *= 1000003;
    h ^= (this.loggerProvider == null) ? 0 : this.loggerProvider.hashCode();
    h *= 1000003;
    h ^= (this.meterProvider == null) ? 0 : this.meterProvider.hashCode();
    h *= 1000003;
    h ^= (this.propagator == null) ? 0 : this.propagator.hashCode();
    h *= 1000003;
    h ^= (this.tracerProvider == null) ? 0 : this.tracerProvider.hashCode();
    h *= 1000003;
    h ^= (this.resource == null) ? 0 : this.resource.hashCode();
    h *= 1000003;
    h ^= (this.instrumentationDevelopment == null) ? 0 : this.instrumentationDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.distribution == null) ? 0 : this.distribution.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof OpenTelemetryConfigurationModel) {
      OpenTelemetryConfigurationModel that = (OpenTelemetryConfigurationModel) o;
      return (this.fileFormat == null
              ? that.fileFormat == null
              : this.fileFormat.equals(that.fileFormat))
          && (this.disabled == null ? that.disabled == null : this.disabled.equals(that.disabled))
          && (this.logLevel == null ? that.logLevel == null : this.logLevel.equals(that.logLevel))
          && (this.attributeLimits == null
              ? that.attributeLimits == null
              : this.attributeLimits.equals(that.attributeLimits))
          && (this.loggerProvider == null
              ? that.loggerProvider == null
              : this.loggerProvider.equals(that.loggerProvider))
          && (this.meterProvider == null
              ? that.meterProvider == null
              : this.meterProvider.equals(that.meterProvider))
          && (this.propagator == null
              ? that.propagator == null
              : this.propagator.equals(that.propagator))
          && (this.tracerProvider == null
              ? that.tracerProvider == null
              : this.tracerProvider.equals(that.tracerProvider))
          && (this.resource == null ? that.resource == null : this.resource.equals(that.resource))
          && (this.instrumentationDevelopment == null
              ? that.instrumentationDevelopment == null
              : this.instrumentationDevelopment.equals(that.instrumentationDevelopment))
          && (this.distribution == null
              ? that.distribution == null
              : this.distribution.equals(that.distribution))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }

  @Generated("jsonschema2pojo")
  public enum SeverityNumber {
    TRACE("trace"),
    TRACE_2("trace2"),
    TRACE_3("trace3"),
    TRACE_4("trace4"),
    DEBUG("debug"),
    DEBUG_2("debug2"),
    DEBUG_3("debug3"),
    DEBUG_4("debug4"),
    INFO("info"),
    INFO_2("info2"),
    INFO_3("info3"),
    INFO_4("info4"),
    WARN("warn"),
    WARN_2("warn2"),
    WARN_3("warn3"),
    WARN_4("warn4"),
    ERROR("error"),
    ERROR_2("error2"),
    ERROR_3("error3"),
    ERROR_4("error4"),
    FATAL("fatal"),
    FATAL_2("fatal2"),
    FATAL_3("fatal3"),
    FATAL_4("fatal4");
    private final String value;
    private static final Map<String, OpenTelemetryConfigurationModel.SeverityNumber> CONSTANTS =
        new HashMap<String, OpenTelemetryConfigurationModel.SeverityNumber>();

    static {
      for (OpenTelemetryConfigurationModel.SeverityNumber c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    SeverityNumber(String value) {
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
    public static OpenTelemetryConfigurationModel.SeverityNumber fromValue(String value) {
      OpenTelemetryConfigurationModel.SeverityNumber constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
