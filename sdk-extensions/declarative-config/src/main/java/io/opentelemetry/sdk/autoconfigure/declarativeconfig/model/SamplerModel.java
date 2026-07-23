/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel.ALWAYS_OFF;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel.ALWAYS_ON;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel.PARENT_BASED;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel.TRACE_ID_RATIO_BASED;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.SamplerModelAccessor.EXPERIMENTAL_PROPERTIES;

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
@JsonPropertyOrder({ALWAYS_OFF, ALWAYS_ON, PARENT_BASED, TRACE_ID_RATIO_BASED})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class SamplerModel {

  static final String ALWAYS_OFF = "always_off";
  static final String ALWAYS_ON = "always_on";
  static final String PARENT_BASED = "parent_based";
  static final String TRACE_ID_RATIO_BASED = "trace_id_ratio_based";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(ALWAYS_OFF, AlwaysOffSamplerModel.class);
    STABLE_PROPERTIES.put(ALWAYS_ON, AlwaysOnSamplerModel.class);
    STABLE_PROPERTIES.put(PARENT_BASED, ParentBasedSamplerModel.class);
    STABLE_PROPERTIES.put(TRACE_ID_RATIO_BASED, TraceIdRatioBasedSamplerModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private AlwaysOffSamplerModel alwaysOff;
  @Nullable private AlwaysOnSamplerModel alwaysOn;
  @Nullable private ParentBasedSamplerModel parentBased;
  @Nullable private TraceIdRatioBasedSamplerModel traceIdRatioBased;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure sampler to be always_off.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(ALWAYS_OFF)
  @Nullable
  public AlwaysOffSamplerModel getAlwaysOff() {
    if (alwaysOff == null) {
      return ExtensionPropertyUtil.getGraduated(
          ALWAYS_OFF, extensionProperties, AlwaysOffSamplerModel.class);
    }
    return alwaysOff;
  }

  @JsonProperty(ALWAYS_OFF)
  public SamplerModel withAlwaysOff(AlwaysOffSamplerModel alwaysOff) {
    this.alwaysOff = alwaysOff;
    return this;
  }

  /**
   * Configure sampler to be always_on.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(ALWAYS_ON)
  @Nullable
  public AlwaysOnSamplerModel getAlwaysOn() {
    if (alwaysOn == null) {
      return ExtensionPropertyUtil.getGraduated(
          ALWAYS_ON, extensionProperties, AlwaysOnSamplerModel.class);
    }
    return alwaysOn;
  }

  @JsonProperty(ALWAYS_ON)
  public SamplerModel withAlwaysOn(AlwaysOnSamplerModel alwaysOn) {
    this.alwaysOn = alwaysOn;
    return this;
  }

  /**
   * Configure sampler to be parent_based.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(PARENT_BASED)
  @Nullable
  public ParentBasedSamplerModel getParentBased() {
    if (parentBased == null) {
      return ExtensionPropertyUtil.getGraduated(
          PARENT_BASED, extensionProperties, ParentBasedSamplerModel.class);
    }
    return parentBased;
  }

  @JsonProperty(PARENT_BASED)
  public SamplerModel withParentBased(ParentBasedSamplerModel parentBased) {
    this.parentBased = parentBased;
    return this;
  }

  /**
   * Configure sampler to be trace_id_ratio_based.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(TRACE_ID_RATIO_BASED)
  @Nullable
  public TraceIdRatioBasedSamplerModel getTraceIdRatioBased() {
    if (traceIdRatioBased == null) {
      return ExtensionPropertyUtil.getGraduated(
          TRACE_ID_RATIO_BASED, extensionProperties, TraceIdRatioBasedSamplerModel.class);
    }
    return traceIdRatioBased;
  }

  @JsonProperty(TRACE_ID_RATIO_BASED)
  public SamplerModel withTraceIdRatioBased(TraceIdRatioBasedSamplerModel traceIdRatioBased) {
    this.traceIdRatioBased = traceIdRatioBased;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public SamplerModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "SamplerModel{"
        + "alwaysOff="
        + alwaysOff
        + ", alwaysOn="
        + alwaysOn
        + ", parentBased="
        + parentBased
        + ", traceIdRatioBased="
        + traceIdRatioBased
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.alwaysOff == null) ? 0 : this.alwaysOff.hashCode();
    h *= 1000003;
    h ^= (this.alwaysOn == null) ? 0 : this.alwaysOn.hashCode();
    h *= 1000003;
    h ^= (this.parentBased == null) ? 0 : this.parentBased.hashCode();
    h *= 1000003;
    h ^= (this.traceIdRatioBased == null) ? 0 : this.traceIdRatioBased.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SamplerModel) {
      SamplerModel that = (SamplerModel) o;
      return (this.alwaysOff == null
              ? that.alwaysOff == null
              : this.alwaysOff.equals(that.alwaysOff))
          && (this.alwaysOn == null ? that.alwaysOn == null : this.alwaysOn.equals(that.alwaysOn))
          && (this.parentBased == null
              ? that.parentBased == null
              : this.parentBased.equals(that.parentBased))
          && (this.traceIdRatioBased == null
              ? that.traceIdRatioBased == null
              : this.traceIdRatioBased.equals(that.traceIdRatioBased))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
