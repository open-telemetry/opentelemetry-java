/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExplicitBucketHistogramAggregationModel.BOUNDARIES;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExplicitBucketHistogramAggregationModel.RECORD_MIN_MAX;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({BOUNDARIES, RECORD_MIN_MAX})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExplicitBucketHistogramAggregationModel {

  static final String BOUNDARIES = "boundaries";
  static final String RECORD_MIN_MAX = "record_min_max";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(RECORD_MIN_MAX, Boolean.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private List<Double> boundaries;
  @Nullable private Boolean recordMinMax;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure bucket boundaries.
   *
   * <p>If omitted, [0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500, 5000, 7500, 10000] is
   * used.
   */
  @JsonProperty(BOUNDARIES)
  @Nullable
  public List<Double> getBoundaries() {
    return boundaries;
  }

  @JsonProperty(BOUNDARIES)
  public ExplicitBucketHistogramAggregationModel withBoundaries(List<Double> boundaries) {
    this.boundaries = boundaries;
    return this;
  }

  /**
   * Configure record min and max.
   *
   * <p>If omitted or null, true is used.
   */
  @JsonProperty(RECORD_MIN_MAX)
  @Nullable
  public Boolean getRecordMinMax() {
    if (recordMinMax == null) {
      return ExtensionPropertyUtil.getGraduated(RECORD_MIN_MAX, extensionProperties, Boolean.class);
    }
    return recordMinMax;
  }

  @JsonProperty(RECORD_MIN_MAX)
  public ExplicitBucketHistogramAggregationModel withRecordMinMax(Boolean recordMinMax) {
    this.recordMinMax = recordMinMax;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public ExplicitBucketHistogramAggregationModel withExtensionProperty(
      String name, @Nullable Object value) {
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
    return "ExplicitBucketHistogramAggregationModel{"
        + "boundaries="
        + boundaries
        + ", recordMinMax="
        + recordMinMax
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.boundaries == null) ? 0 : this.boundaries.hashCode();
    h *= 1000003;
    h ^= (this.recordMinMax == null) ? 0 : this.recordMinMax.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExplicitBucketHistogramAggregationModel) {
      ExplicitBucketHistogramAggregationModel that = (ExplicitBucketHistogramAggregationModel) o;
      return (this.boundaries == null
              ? that.boundaries == null
              : this.boundaries.equals(that.boundaries))
          && (this.recordMinMax == null
              ? that.recordMinMax == null
              : this.recordMinMax.equals(that.recordMinMax))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
