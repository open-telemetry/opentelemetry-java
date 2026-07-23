/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricReaderModel.PERIODIC;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricReaderModel.PULL;

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
@JsonPropertyOrder({PERIODIC, PULL})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class MetricReaderModel {

  static final String PERIODIC = "periodic";
  static final String PULL = "pull";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(PERIODIC, PeriodicMetricReaderModel.class);
    STABLE_PROPERTIES.put(PULL, PullMetricReaderModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private PeriodicMetricReaderModel periodic;
  @Nullable private PullMetricReaderModel pull;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure a periodic metric reader.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(PERIODIC)
  @Nullable
  public PeriodicMetricReaderModel getPeriodic() {
    if (periodic == null) {
      return ExtensionPropertyUtil.getGraduated(
          PERIODIC, extensionProperties, PeriodicMetricReaderModel.class);
    }
    return periodic;
  }

  @JsonProperty(PERIODIC)
  public MetricReaderModel withPeriodic(PeriodicMetricReaderModel periodic) {
    this.periodic = periodic;
    return this;
  }

  /**
   * Configure a pull based metric reader.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(PULL)
  @Nullable
  public PullMetricReaderModel getPull() {
    if (pull == null) {
      return ExtensionPropertyUtil.getGraduated(
          PULL, extensionProperties, PullMetricReaderModel.class);
    }
    return pull;
  }

  @JsonProperty(PULL)
  public MetricReaderModel withPull(PullMetricReaderModel pull) {
    this.pull = pull;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public MetricReaderModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "MetricReaderModel{"
        + "periodic="
        + periodic
        + ", pull="
        + pull
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.periodic == null) ? 0 : this.periodic.hashCode();
    h *= 1000003;
    h ^= (this.pull == null) ? 0 : this.pull.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MetricReaderModel) {
      MetricReaderModel that = (MetricReaderModel) o;
      return (this.periodic == null ? that.periodic == null : this.periodic.equals(that.periodic))
          && (this.pull == null ? that.pull == null : this.pull.equals(that.pull))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
