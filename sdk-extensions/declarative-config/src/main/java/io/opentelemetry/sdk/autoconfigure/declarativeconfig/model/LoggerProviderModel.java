/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LoggerProviderModel.LIMITS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LoggerProviderModel.PROCESSORS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.LoggerProviderModelAccessor.EXPERIMENTAL_PROPERTIES;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({PROCESSORS, LIMITS})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class LoggerProviderModel {

  static final String PROCESSORS = "processors";
  static final String LIMITS = "limits";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(LIMITS, LogRecordLimitsModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private List<LogRecordProcessorModel> processors;
  @Nullable private LogRecordLimitsModel limits;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure log record processors.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(PROCESSORS)
  @Nullable
  public List<LogRecordProcessorModel> getProcessors() {
    return processors;
  }

  @JsonProperty(PROCESSORS)
  public LoggerProviderModel withProcessors(List<LogRecordProcessorModel> processors) {
    this.processors = processors;
    return this;
  }

  /**
   * Configure log record limits. See also attribute_limits.
   *
   * <p>If omitted, default values as described in LogRecordLimits are used.
   */
  @JsonProperty(LIMITS)
  @Nullable
  public LogRecordLimitsModel getLimits() {
    if (limits == null) {
      return ExtensionPropertyUtil.getGraduated(
          LIMITS, extensionProperties, LogRecordLimitsModel.class);
    }
    return limits;
  }

  @JsonProperty(LIMITS)
  public LoggerProviderModel withLimits(LogRecordLimitsModel limits) {
    this.limits = limits;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public LoggerProviderModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "LoggerProviderModel{"
        + "processors="
        + processors
        + ", limits="
        + limits
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.processors == null) ? 0 : this.processors.hashCode();
    h *= 1000003;
    h ^= (this.limits == null) ? 0 : this.limits.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof LoggerProviderModel) {
      LoggerProviderModel that = (LoggerProviderModel) o;
      return (this.processors == null
              ? that.processors == null
              : this.processors.equals(that.processors))
          && (this.limits == null ? that.limits == null : this.limits.equals(that.limits))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
