/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TraceIdRatioBasedSamplerModel.RATIO;

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
@JsonPropertyOrder({RATIO})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class TraceIdRatioBasedSamplerModel {

  static final String RATIO = "ratio";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(RATIO, Double.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private Double ratio;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure trace_id_ratio.
   *
   * <p>If omitted or null, 1.0 is used.
   */
  @JsonProperty(RATIO)
  @Nullable
  public Double getRatio() {
    if (ratio == null) {
      return ExtensionPropertyUtil.getGraduated(RATIO, extensionProperties, Double.class);
    }
    return ratio;
  }

  @JsonProperty(RATIO)
  public TraceIdRatioBasedSamplerModel withRatio(Double ratio) {
    this.ratio = ratio;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public TraceIdRatioBasedSamplerModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "TraceIdRatioBasedSamplerModel{"
        + "ratio="
        + ratio
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.ratio == null) ? 0 : this.ratio.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TraceIdRatioBasedSamplerModel) {
      TraceIdRatioBasedSamplerModel that = (TraceIdRatioBasedSamplerModel) o;
      return (this.ratio == null ? that.ratio == null : this.ratio.equals(that.ratio))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
