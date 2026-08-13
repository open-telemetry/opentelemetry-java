/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricProducerModel.OPENCENSUS;

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
@JsonPropertyOrder({OPENCENSUS})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class MetricProducerModel {

  static final String OPENCENSUS = "opencensus";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(OPENCENSUS, OpenCensusMetricProducerModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private OpenCensusMetricProducerModel opencensus;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure metric producer to be opencensus.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(OPENCENSUS)
  @Nullable
  public OpenCensusMetricProducerModel getOpencensus() {
    if (opencensus == null) {
      return ExtensionPropertyUtil.getGraduated(
          OPENCENSUS, extensionProperties, OpenCensusMetricProducerModel.class);
    }
    return opencensus;
  }

  @JsonProperty(OPENCENSUS)
  public MetricProducerModel withOpencensus(OpenCensusMetricProducerModel opencensus) {
    this.opencensus = opencensus;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public MetricProducerModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "MetricProducerModel{"
        + "opencensus="
        + opencensus
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.getOpencensus() == null) ? 0 : this.getOpencensus().hashCode();
    h *= 1000003;
    h ^= (this.getExtensionProperties() == null) ? 0 : this.getExtensionProperties().hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MetricProducerModel) {
      MetricProducerModel that = (MetricProducerModel) o;
      return (this.getOpencensus() == null
              ? that.getOpencensus() == null
              : this.getOpencensus().equals(that.getOpencensus()))
          && (this.getExtensionProperties() == null
              ? that.getExtensionProperties() == null
              : this.getExtensionProperties().equals(that.getExtensionProperties()));
    }
    return false;
  }
}
