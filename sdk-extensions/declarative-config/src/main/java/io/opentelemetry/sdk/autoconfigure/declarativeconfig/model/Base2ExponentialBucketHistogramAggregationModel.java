/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.Base2ExponentialBucketHistogramAggregationModel.MAX_SCALE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.Base2ExponentialBucketHistogramAggregationModel.MAX_SIZE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.Base2ExponentialBucketHistogramAggregationModel.RECORD_MIN_MAX;

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
@JsonPropertyOrder({MAX_SCALE, MAX_SIZE, RECORD_MIN_MAX})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class Base2ExponentialBucketHistogramAggregationModel {

  static final String MAX_SCALE = "max_scale";
  static final String MAX_SIZE = "max_size";
  static final String RECORD_MIN_MAX = "record_min_max";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(MAX_SCALE, Integer.class);
    STABLE_PROPERTIES.put(MAX_SIZE, Integer.class);
    STABLE_PROPERTIES.put(RECORD_MIN_MAX, Boolean.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private Integer maxScale;
  @Nullable private Integer maxSize;
  @Nullable private Boolean recordMinMax;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure the max scale factor.
   *
   * <p>If omitted or null, 20 is used.
   */
  @JsonProperty(MAX_SCALE)
  @Nullable
  public Integer getMaxScale() {
    if (maxScale == null) {
      return ExtensionPropertyUtil.getGraduated(MAX_SCALE, extensionProperties, Integer.class);
    }
    return maxScale;
  }

  @JsonProperty(MAX_SCALE)
  public Base2ExponentialBucketHistogramAggregationModel withMaxScale(Integer maxScale) {
    this.maxScale = maxScale;
    return this;
  }

  /**
   * Configure the maximum number of buckets in each of the positive and negative ranges, not
   * counting the special zero bucket.
   *
   * <p>If omitted or null, 160 is used.
   */
  @JsonProperty(MAX_SIZE)
  @Nullable
  public Integer getMaxSize() {
    if (maxSize == null) {
      return ExtensionPropertyUtil.getGraduated(MAX_SIZE, extensionProperties, Integer.class);
    }
    return maxSize;
  }

  @JsonProperty(MAX_SIZE)
  public Base2ExponentialBucketHistogramAggregationModel withMaxSize(Integer maxSize) {
    this.maxSize = maxSize;
    return this;
  }

  /**
   * Configure whether or not to record min and max.
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
  public Base2ExponentialBucketHistogramAggregationModel withRecordMinMax(Boolean recordMinMax) {
    this.recordMinMax = recordMinMax;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public Base2ExponentialBucketHistogramAggregationModel withExtensionProperty(
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
    return "Base2ExponentialBucketHistogramAggregationModel{"
        + "maxScale="
        + maxScale
        + ", maxSize="
        + maxSize
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
    h ^= (this.maxScale == null) ? 0 : this.maxScale.hashCode();
    h *= 1000003;
    h ^= (this.maxSize == null) ? 0 : this.maxSize.hashCode();
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
    if (o instanceof Base2ExponentialBucketHistogramAggregationModel) {
      Base2ExponentialBucketHistogramAggregationModel that =
          (Base2ExponentialBucketHistogramAggregationModel) o;
      return (this.maxScale == null ? that.maxScale == null : this.maxScale.equals(that.maxScale))
          && (this.maxSize == null ? that.maxSize == null : this.maxSize.equals(that.maxSize))
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
