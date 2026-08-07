/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel.BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel.DEFAULT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel.DROP;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel.EXPLICIT_BUCKET_HISTOGRAM;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel.LAST_VALUE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel.SUM;

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
@JsonPropertyOrder({
  DEFAULT,
  DROP,
  EXPLICIT_BUCKET_HISTOGRAM,
  BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM,
  LAST_VALUE,
  SUM
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class AggregationModel {

  static final String DEFAULT = "default";
  static final String DROP = "drop";
  static final String EXPLICIT_BUCKET_HISTOGRAM = "explicit_bucket_histogram";
  static final String BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM = "base2_exponential_bucket_histogram";
  static final String LAST_VALUE = "last_value";
  static final String SUM = "sum";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(DEFAULT, DefaultAggregationModel.class);
    STABLE_PROPERTIES.put(DROP, DropAggregationModel.class);
    STABLE_PROPERTIES.put(EXPLICIT_BUCKET_HISTOGRAM, ExplicitBucketHistogramAggregationModel.class);
    STABLE_PROPERTIES.put(
        BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM, Base2ExponentialBucketHistogramAggregationModel.class);
    STABLE_PROPERTIES.put(LAST_VALUE, LastValueAggregationModel.class);
    STABLE_PROPERTIES.put(SUM, SumAggregationModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private DefaultAggregationModel _default;
  @Nullable private DropAggregationModel drop;
  @Nullable private ExplicitBucketHistogramAggregationModel explicitBucketHistogram;
  @Nullable private Base2ExponentialBucketHistogramAggregationModel base2ExponentialBucketHistogram;
  @Nullable private LastValueAggregationModel lastValue;
  @Nullable private SumAggregationModel sum;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configures the stream to use the instrument kind to select an aggregation and advisory
   * parameters to influence aggregation configuration parameters. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#default-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(DEFAULT)
  @Nullable
  public DefaultAggregationModel getDefault() {
    if (_default == null) {
      return ExtensionPropertyUtil.getGraduated(
          DEFAULT, extensionProperties, DefaultAggregationModel.class);
    }
    return _default;
  }

  @JsonProperty(DEFAULT)
  public AggregationModel withDefault(DefaultAggregationModel _default) {
    this._default = _default;
    return this;
  }

  /**
   * Configures the stream to ignore/drop all instrument measurements. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#drop-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(DROP)
  @Nullable
  public DropAggregationModel getDrop() {
    if (drop == null) {
      return ExtensionPropertyUtil.getGraduated(
          DROP, extensionProperties, DropAggregationModel.class);
    }
    return drop;
  }

  @JsonProperty(DROP)
  public AggregationModel withDrop(DropAggregationModel drop) {
    this.drop = drop;
    return this;
  }

  /**
   * Configures the stream to collect data for the histogram metric point using a set of explicit
   * boundary values for histogram bucketing. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#explicit-bucket-histogram-aggregation
   * for details
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(EXPLICIT_BUCKET_HISTOGRAM)
  @Nullable
  public ExplicitBucketHistogramAggregationModel getExplicitBucketHistogram() {
    if (explicitBucketHistogram == null) {
      return ExtensionPropertyUtil.getGraduated(
          EXPLICIT_BUCKET_HISTOGRAM,
          extensionProperties,
          ExplicitBucketHistogramAggregationModel.class);
    }
    return explicitBucketHistogram;
  }

  @JsonProperty(EXPLICIT_BUCKET_HISTOGRAM)
  public AggregationModel withExplicitBucketHistogram(
      ExplicitBucketHistogramAggregationModel explicitBucketHistogram) {
    this.explicitBucketHistogram = explicitBucketHistogram;
    return this;
  }

  /**
   * Configures the stream to collect data for the exponential histogram metric point, which uses a
   * base-2 exponential formula to determine bucket boundaries and an integer scale parameter to
   * control resolution. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#base2-exponential-bucket-histogram-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM)
  @Nullable
  public Base2ExponentialBucketHistogramAggregationModel getBase2ExponentialBucketHistogram() {
    if (base2ExponentialBucketHistogram == null) {
      return ExtensionPropertyUtil.getGraduated(
          BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM,
          extensionProperties,
          Base2ExponentialBucketHistogramAggregationModel.class);
    }
    return base2ExponentialBucketHistogram;
  }

  @JsonProperty(BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM)
  public AggregationModel withBase2ExponentialBucketHistogram(
      Base2ExponentialBucketHistogramAggregationModel base2ExponentialBucketHistogram) {
    this.base2ExponentialBucketHistogram = base2ExponentialBucketHistogram;
    return this;
  }

  /**
   * Configures the stream to collect data using the last measurement. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#last-value-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(LAST_VALUE)
  @Nullable
  public LastValueAggregationModel getLastValue() {
    if (lastValue == null) {
      return ExtensionPropertyUtil.getGraduated(
          LAST_VALUE, extensionProperties, LastValueAggregationModel.class);
    }
    return lastValue;
  }

  @JsonProperty(LAST_VALUE)
  public AggregationModel withLastValue(LastValueAggregationModel lastValue) {
    this.lastValue = lastValue;
    return this;
  }

  /**
   * Configures the stream to collect the arithmetic sum of measurement values. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#sum-aggregation
   * for details.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(SUM)
  @Nullable
  public SumAggregationModel getSum() {
    if (sum == null) {
      return ExtensionPropertyUtil.getGraduated(
          SUM, extensionProperties, SumAggregationModel.class);
    }
    return sum;
  }

  @JsonProperty(SUM)
  public AggregationModel withSum(SumAggregationModel sum) {
    this.sum = sum;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public AggregationModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "AggregationModel{"
        + "_default="
        + _default
        + ", drop="
        + drop
        + ", explicitBucketHistogram="
        + explicitBucketHistogram
        + ", base2ExponentialBucketHistogram="
        + base2ExponentialBucketHistogram
        + ", lastValue="
        + lastValue
        + ", sum="
        + sum
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this._default == null) ? 0 : this._default.hashCode();
    h *= 1000003;
    h ^= (this.drop == null) ? 0 : this.drop.hashCode();
    h *= 1000003;
    h ^= (this.explicitBucketHistogram == null) ? 0 : this.explicitBucketHistogram.hashCode();
    h *= 1000003;
    h ^=
        (this.base2ExponentialBucketHistogram == null)
            ? 0
            : this.base2ExponentialBucketHistogram.hashCode();
    h *= 1000003;
    h ^= (this.lastValue == null) ? 0 : this.lastValue.hashCode();
    h *= 1000003;
    h ^= (this.sum == null) ? 0 : this.sum.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AggregationModel) {
      AggregationModel that = (AggregationModel) o;
      return (this._default == null ? that._default == null : this._default.equals(that._default))
          && (this.drop == null ? that.drop == null : this.drop.equals(that.drop))
          && (this.explicitBucketHistogram == null
              ? that.explicitBucketHistogram == null
              : this.explicitBucketHistogram.equals(that.explicitBucketHistogram))
          && (this.base2ExponentialBucketHistogram == null
              ? that.base2ExponentialBucketHistogram == null
              : this.base2ExponentialBucketHistogram.equals(that.base2ExponentialBucketHistogram))
          && (this.lastValue == null
              ? that.lastValue == null
              : this.lastValue.equals(that.lastValue))
          && (this.sum == null ? that.sum == null : this.sum.equals(that.sum))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
