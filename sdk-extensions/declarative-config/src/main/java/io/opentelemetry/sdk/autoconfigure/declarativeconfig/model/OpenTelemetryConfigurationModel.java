/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class OpenTelemetryConfigurationModel {

  /**
   * The file format version. Represented as a string including the semver major, minor version
   * numbers (and optionally the meta tag). For example: "0.4", "1.0-rc.2", "1.0" (after stable
   * release). See
   * https://github.com/open-telemetry/opentelemetry-configuration/blob/main/VERSIONING.md for more
   * details. The yaml format is documented at
   * https://github.com/open-telemetry/opentelemetry-configuration/tree/main/schema Property is
   * required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("file_format")
  @JsonPropertyDescription(
      "The file format version.\nRepresented as a string including the semver major, minor version numbers (and optionally the meta tag). For example: \"0.4\", \"1.0-rc.2\", \"1.0\" (after stable release).\nSee https://github.com/open-telemetry/opentelemetry-configuration/blob/main/VERSIONING.md for more details.\nThe yaml format is documented at https://github.com/open-telemetry/opentelemetry-configuration/tree/main/schema\nProperty is required and must be non-null.\n")
  @Nonnull
  private String fileFormat;

  /**
   * Configure if the SDK is disabled or not. If omitted or null, false is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("disabled")
  @JsonPropertyDescription(
      "Configure if the SDK is disabled or not.\nIf omitted or null, false is used.\n")
  private Boolean disabled;

  /** (Can be null) */
  @Nullable
  @JsonProperty("log_level")
  private OpenTelemetryConfigurationModel.SeverityNumber logLevel;

  /** (Can be null) */
  @Nullable
  @JsonProperty("attribute_limits")
  private AttributeLimitsModel attributeLimits;

  @Nullable
  @JsonProperty("logger_provider")
  private LoggerProviderModel loggerProvider;

  @Nullable
  @JsonProperty("meter_provider")
  private MeterProviderModel meterProvider;

  /** (Can be null) */
  @Nullable
  @JsonProperty("propagator")
  private PropagatorModel propagator;

  @Nullable
  @JsonProperty("tracer_provider")
  private TracerProviderModel tracerProvider;

  /** (Can be null) */
  @Nullable
  @JsonProperty("resource")
  private ResourceModel resource;

  /** (Can be null) */
  @Nullable
  @JsonProperty("instrumentation/development")
  private ExperimentalInstrumentationModel instrumentationDevelopment;

  /** (Can be null) */
  @Nullable
  @JsonProperty("distribution")
  private DistributionModel distribution;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

  /**
   * The file format version. Represented as a string including the semver major, minor version
   * numbers (and optionally the meta tag). For example: "0.4", "1.0-rc.2", "1.0" (after stable
   * release). See
   * https://github.com/open-telemetry/opentelemetry-configuration/blob/main/VERSIONING.md for more
   * details. The yaml format is documented at
   * https://github.com/open-telemetry/opentelemetry-configuration/tree/main/schema Property is
   * required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("file_format")
  @Nullable
  public String getFileFormat() {
    return fileFormat;
  }

  public OpenTelemetryConfigurationModel withFileFormat(String fileFormat) {
    this.fileFormat = fileFormat;
    return this;
  }

  /** Configure if the SDK is disabled or not. If omitted or null, false is used. */
  @JsonProperty("disabled")
  @Nullable
  public Boolean getDisabled() {
    return disabled;
  }

  public OpenTelemetryConfigurationModel withDisabled(Boolean disabled) {
    this.disabled = disabled;
    return this;
  }

  @JsonProperty("log_level")
  @Nullable
  public OpenTelemetryConfigurationModel.SeverityNumber getLogLevel() {
    return logLevel;
  }

  public OpenTelemetryConfigurationModel withLogLevel(
      OpenTelemetryConfigurationModel.SeverityNumber logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  @JsonProperty("attribute_limits")
  @Nullable
  public AttributeLimitsModel getAttributeLimits() {
    return attributeLimits;
  }

  public OpenTelemetryConfigurationModel withAttributeLimits(AttributeLimitsModel attributeLimits) {
    this.attributeLimits = attributeLimits;
    return this;
  }

  @JsonProperty("logger_provider")
  @Nullable
  public LoggerProviderModel getLoggerProvider() {
    return loggerProvider;
  }

  public OpenTelemetryConfigurationModel withLoggerProvider(LoggerProviderModel loggerProvider) {
    this.loggerProvider = loggerProvider;
    return this;
  }

  @JsonProperty("meter_provider")
  @Nullable
  public MeterProviderModel getMeterProvider() {
    return meterProvider;
  }

  public OpenTelemetryConfigurationModel withMeterProvider(MeterProviderModel meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  @JsonProperty("propagator")
  @Nullable
  public PropagatorModel getPropagator() {
    return propagator;
  }

  public OpenTelemetryConfigurationModel withPropagator(PropagatorModel propagator) {
    this.propagator = propagator;
    return this;
  }

  @JsonProperty("tracer_provider")
  @Nullable
  public TracerProviderModel getTracerProvider() {
    return tracerProvider;
  }

  public OpenTelemetryConfigurationModel withTracerProvider(TracerProviderModel tracerProvider) {
    this.tracerProvider = tracerProvider;
    return this;
  }

  @JsonProperty("resource")
  @Nullable
  public ResourceModel getResource() {
    return resource;
  }

  public OpenTelemetryConfigurationModel withResource(ResourceModel resource) {
    this.resource = resource;
    return this;
  }

  @JsonProperty("instrumentation/development")
  @Nullable
  public ExperimentalInstrumentationModel getInstrumentationDevelopment() {
    return instrumentationDevelopment;
  }

  public OpenTelemetryConfigurationModel withInstrumentationDevelopment(
      ExperimentalInstrumentationModel instrumentationDevelopment) {
    this.instrumentationDevelopment = instrumentationDevelopment;
    return this;
  }

  @JsonProperty("distribution")
  @Nullable
  public DistributionModel getDistribution() {
    return distribution;
  }

  public OpenTelemetryConfigurationModel withDistribution(DistributionModel distribution) {
    this.distribution = distribution;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public OpenTelemetryConfigurationModel withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(OpenTelemetryConfigurationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("fileFormat");
    sb.append('=');
    sb.append(((this.fileFormat == null) ? "<null>" : this.fileFormat));
    sb.append(',');
    sb.append("disabled");
    sb.append('=');
    sb.append(((this.disabled == null) ? "<null>" : this.disabled));
    sb.append(',');
    sb.append("logLevel");
    sb.append('=');
    sb.append(((this.logLevel == null) ? "<null>" : this.logLevel));
    sb.append(',');
    sb.append("attributeLimits");
    sb.append('=');
    sb.append(((this.attributeLimits == null) ? "<null>" : this.attributeLimits));
    sb.append(',');
    sb.append("loggerProvider");
    sb.append('=');
    sb.append(((this.loggerProvider == null) ? "<null>" : this.loggerProvider));
    sb.append(',');
    sb.append("meterProvider");
    sb.append('=');
    sb.append(((this.meterProvider == null) ? "<null>" : this.meterProvider));
    sb.append(',');
    sb.append("propagator");
    sb.append('=');
    sb.append(((this.propagator == null) ? "<null>" : this.propagator));
    sb.append(',');
    sb.append("tracerProvider");
    sb.append('=');
    sb.append(((this.tracerProvider == null) ? "<null>" : this.tracerProvider));
    sb.append(',');
    sb.append("resource");
    sb.append('=');
    sb.append(((this.resource == null) ? "<null>" : this.resource));
    sb.append(',');
    sb.append("instrumentationDevelopment");
    sb.append('=');
    sb.append(
        ((this.instrumentationDevelopment == null) ? "<null>" : this.instrumentationDevelopment));
    sb.append(',');
    sb.append("distribution");
    sb.append('=');
    sb.append(((this.distribution == null) ? "<null>" : this.distribution));
    sb.append(',');
    sb.append("additionalProperties");
    sb.append('=');
    sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
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
    result =
        ((result * 31)
            + ((this.instrumentationDevelopment == null)
                ? 0
                : this.instrumentationDevelopment.hashCode()));
    result =
        ((result * 31) + ((this.attributeLimits == null) ? 0 : this.attributeLimits.hashCode()));
    result = ((result * 31) + ((this.logLevel == null) ? 0 : this.logLevel.hashCode()));
    result = ((result * 31) + ((this.tracerProvider == null) ? 0 : this.tracerProvider.hashCode()));
    result = ((result * 31) + ((this.resource == null) ? 0 : this.resource.hashCode()));
    result = ((result * 31) + ((this.meterProvider == null) ? 0 : this.meterProvider.hashCode()));
    result = ((result * 31) + ((this.propagator == null) ? 0 : this.propagator.hashCode()));
    result = ((result * 31) + ((this.disabled == null) ? 0 : this.disabled.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    result = ((result * 31) + ((this.distribution == null) ? 0 : this.distribution.hashCode()));
    result = ((result * 31) + ((this.fileFormat == null) ? 0 : this.fileFormat.hashCode()));
    result = ((result * 31) + ((this.loggerProvider == null) ? 0 : this.loggerProvider.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof OpenTelemetryConfigurationModel) == false) {
      return false;
    }
    OpenTelemetryConfigurationModel rhs = ((OpenTelemetryConfigurationModel) other);
    return (((((((((((((this.instrumentationDevelopment == rhs.instrumentationDevelopment)
                                                    || ((this.instrumentationDevelopment != null)
                                                        && this.instrumentationDevelopment.equals(
                                                            rhs.instrumentationDevelopment)))
                                                && ((this.attributeLimits == rhs.attributeLimits)
                                                    || ((this.attributeLimits != null)
                                                        && this.attributeLimits.equals(
                                                            rhs.attributeLimits))))
                                            && ((this.logLevel == rhs.logLevel)
                                                || ((this.logLevel != null)
                                                    && this.logLevel.equals(rhs.logLevel))))
                                        && ((this.tracerProvider == rhs.tracerProvider)
                                            || ((this.tracerProvider != null)
                                                && this.tracerProvider.equals(rhs.tracerProvider))))
                                    && ((this.resource == rhs.resource)
                                        || ((this.resource != null)
                                            && this.resource.equals(rhs.resource))))
                                && ((this.meterProvider == rhs.meterProvider)
                                    || ((this.meterProvider != null)
                                        && this.meterProvider.equals(rhs.meterProvider))))
                            && ((this.propagator == rhs.propagator)
                                || ((this.propagator != null)
                                    && this.propagator.equals(rhs.propagator))))
                        && ((this.disabled == rhs.disabled)
                            || ((this.disabled != null) && this.disabled.equals(rhs.disabled))))
                    && ((this.additionalProperties == rhs.additionalProperties)
                        || ((this.additionalProperties != null)
                            && this.additionalProperties.equals(rhs.additionalProperties))))
                && ((this.distribution == rhs.distribution)
                    || ((this.distribution != null) && this.distribution.equals(rhs.distribution))))
            && ((this.fileFormat == rhs.fileFormat)
                || ((this.fileFormat != null) && this.fileFormat.equals(rhs.fileFormat))))
        && ((this.loggerProvider == rhs.loggerProvider)
            || ((this.loggerProvider != null) && this.loggerProvider.equals(rhs.loggerProvider))));
  }

  @Generated("jsonschema2pojo")
  @SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
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
