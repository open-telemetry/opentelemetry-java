/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.ATTRIBUTE_LIMITS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.DISABLED;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.DISTRIBUTION;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.FILE_FORMAT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.LOGGER_PROVIDER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.LOG_LEVEL;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.METER_PROVIDER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.PROPAGATOR;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.RESOURCE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel.TRACER_PROVIDER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.OpenTelemetryConfigurationModelAccessor.EXPERIMENTAL_PROPERTIES;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  FILE_FORMAT,
  DISABLED,
  LOG_LEVEL,
  ATTRIBUTE_LIMITS,
  LOGGER_PROVIDER,
  METER_PROVIDER,
  PROPAGATOR,
  TRACER_PROVIDER,
  RESOURCE,
  DISTRIBUTION
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class OpenTelemetryConfigurationModel {

  static final String FILE_FORMAT = "file_format";
  static final String DISABLED = "disabled";
  static final String LOG_LEVEL = "log_level";
  static final String ATTRIBUTE_LIMITS = "attribute_limits";
  static final String LOGGER_PROVIDER = "logger_provider";
  static final String METER_PROVIDER = "meter_provider";
  static final String PROPAGATOR = "propagator";
  static final String TRACER_PROVIDER = "tracer_provider";
  static final String RESOURCE = "resource";
  static final String DISTRIBUTION = "distribution";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(FILE_FORMAT, String.class);
    STABLE_PROPERTIES.put(DISABLED, Boolean.class);
    STABLE_PROPERTIES.put(LOG_LEVEL, SeverityNumberModel.class);
    STABLE_PROPERTIES.put(ATTRIBUTE_LIMITS, AttributeLimitsModel.class);
    STABLE_PROPERTIES.put(LOGGER_PROVIDER, LoggerProviderModel.class);
    STABLE_PROPERTIES.put(METER_PROVIDER, MeterProviderModel.class);
    STABLE_PROPERTIES.put(PROPAGATOR, PropagatorModel.class);
    STABLE_PROPERTIES.put(TRACER_PROVIDER, TracerProviderModel.class);
    STABLE_PROPERTIES.put(RESOURCE, ResourceModel.class);
    STABLE_PROPERTIES.put(DISTRIBUTION, DistributionModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private String fileFormat;
  @Nullable private Boolean disabled;
  @Nullable private SeverityNumberModel logLevel;
  @Nullable private AttributeLimitsModel attributeLimits;
  @Nullable private LoggerProviderModel loggerProvider;
  @Nullable private MeterProviderModel meterProvider;
  @Nullable private PropagatorModel propagator;
  @Nullable private TracerProviderModel tracerProvider;
  @Nullable private ResourceModel resource;
  @Nullable private DistributionModel distribution;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

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
  @JsonProperty(FILE_FORMAT)
  @Nullable
  public String getFileFormat() {
    if (fileFormat == null) {
      return ExtensionPropertyUtil.getGraduated(FILE_FORMAT, extensionProperties, String.class);
    }
    return fileFormat;
  }

  @JsonProperty(FILE_FORMAT)
  public OpenTelemetryConfigurationModel withFileFormat(String fileFormat) {
    this.fileFormat = fileFormat;
    return this;
  }

  /**
   * Configure if the SDK is disabled or not.
   *
   * <p>If omitted or null, false is used.
   */
  @JsonProperty(DISABLED)
  @Nullable
  public Boolean getDisabled() {
    if (disabled == null) {
      return ExtensionPropertyUtil.getGraduated(DISABLED, extensionProperties, Boolean.class);
    }
    return disabled;
  }

  @JsonProperty(DISABLED)
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
  @JsonProperty(LOG_LEVEL)
  @Nullable
  public SeverityNumberModel getLogLevel() {
    if (logLevel == null) {
      return ExtensionPropertyUtil.getGraduated(
          LOG_LEVEL, extensionProperties, SeverityNumberModel.class);
    }
    return logLevel;
  }

  @JsonProperty(LOG_LEVEL)
  public OpenTelemetryConfigurationModel withLogLevel(SeverityNumberModel logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  /**
   * Configure general attribute limits. See also tracer_provider.limits, logger_provider.limits.
   *
   * <p>If omitted, default values as described in AttributeLimits are used.
   */
  @JsonProperty(ATTRIBUTE_LIMITS)
  @Nullable
  public AttributeLimitsModel getAttributeLimits() {
    if (attributeLimits == null) {
      return ExtensionPropertyUtil.getGraduated(
          ATTRIBUTE_LIMITS, extensionProperties, AttributeLimitsModel.class);
    }
    return attributeLimits;
  }

  @JsonProperty(ATTRIBUTE_LIMITS)
  public OpenTelemetryConfigurationModel withAttributeLimits(AttributeLimitsModel attributeLimits) {
    this.attributeLimits = attributeLimits;
    return this;
  }

  /**
   * Configure logger provider.
   *
   * <p>If omitted, a noop logger provider is used.
   */
  @JsonProperty(LOGGER_PROVIDER)
  @Nullable
  public LoggerProviderModel getLoggerProvider() {
    if (loggerProvider == null) {
      return ExtensionPropertyUtil.getGraduated(
          LOGGER_PROVIDER, extensionProperties, LoggerProviderModel.class);
    }
    return loggerProvider;
  }

  @JsonProperty(LOGGER_PROVIDER)
  public OpenTelemetryConfigurationModel withLoggerProvider(LoggerProviderModel loggerProvider) {
    this.loggerProvider = loggerProvider;
    return this;
  }

  /**
   * Configure meter provider.
   *
   * <p>If omitted, a noop meter provider is used.
   */
  @JsonProperty(METER_PROVIDER)
  @Nullable
  public MeterProviderModel getMeterProvider() {
    if (meterProvider == null) {
      return ExtensionPropertyUtil.getGraduated(
          METER_PROVIDER, extensionProperties, MeterProviderModel.class);
    }
    return meterProvider;
  }

  @JsonProperty(METER_PROVIDER)
  public OpenTelemetryConfigurationModel withMeterProvider(MeterProviderModel meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  /**
   * Configure text map context propagators.
   *
   * <p>If omitted, a noop propagator is used.
   */
  @JsonProperty(PROPAGATOR)
  @Nullable
  public PropagatorModel getPropagator() {
    if (propagator == null) {
      return ExtensionPropertyUtil.getGraduated(
          PROPAGATOR, extensionProperties, PropagatorModel.class);
    }
    return propagator;
  }

  @JsonProperty(PROPAGATOR)
  public OpenTelemetryConfigurationModel withPropagator(PropagatorModel propagator) {
    this.propagator = propagator;
    return this;
  }

  /**
   * Configure tracer provider.
   *
   * <p>If omitted, a noop tracer provider is used.
   */
  @JsonProperty(TRACER_PROVIDER)
  @Nullable
  public TracerProviderModel getTracerProvider() {
    if (tracerProvider == null) {
      return ExtensionPropertyUtil.getGraduated(
          TRACER_PROVIDER, extensionProperties, TracerProviderModel.class);
    }
    return tracerProvider;
  }

  @JsonProperty(TRACER_PROVIDER)
  public OpenTelemetryConfigurationModel withTracerProvider(TracerProviderModel tracerProvider) {
    this.tracerProvider = tracerProvider;
    return this;
  }

  /**
   * Configure resource for all signals.
   *
   * <p>If omitted, the default resource is used.
   */
  @JsonProperty(RESOURCE)
  @Nullable
  public ResourceModel getResource() {
    if (resource == null) {
      return ExtensionPropertyUtil.getGraduated(RESOURCE, extensionProperties, ResourceModel.class);
    }
    return resource;
  }

  @JsonProperty(RESOURCE)
  public OpenTelemetryConfigurationModel withResource(ResourceModel resource) {
    this.resource = resource;
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
  @JsonProperty(DISTRIBUTION)
  @Nullable
  public DistributionModel getDistribution() {
    if (distribution == null) {
      return ExtensionPropertyUtil.getGraduated(
          DISTRIBUTION, extensionProperties, DistributionModel.class);
    }
    return distribution;
  }

  @JsonProperty(DISTRIBUTION)
  public OpenTelemetryConfigurationModel withDistribution(DistributionModel distribution) {
    this.distribution = distribution;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public OpenTelemetryConfigurationModel withExtensionProperty(
      String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        EXPERIMENTAL_PROPERTIES,
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
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
        + ", distribution="
        + distribution
        + ", extensionProperties="
        + extensionProperties
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
    h ^= (this.distribution == null) ? 0 : this.distribution.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
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
          && (this.distribution == null
              ? that.distribution == null
              : this.distribution.equals(that.distribution))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
