/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"opencensus"})
@Generated("jsonschema2pojo")
public class MetricProducerModel {

  @Nullable private OpenCensusMetricProducerModel opencensus;
  private Map<String, MetricProducerPropertyModel> additionalProperties =
      new LinkedHashMap<String, MetricProducerPropertyModel>();

  /**
   * Configure metric producer to be opencensus.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("opencensus")
  @Nullable
  public OpenCensusMetricProducerModel getOpencensus() {
    return opencensus;
  }

  @JsonProperty("opencensus")
  public MetricProducerModel withOpencensus(OpenCensusMetricProducerModel opencensus) {
    this.opencensus = opencensus;
    return this;
  }

  @JsonAnyGetter
  public Map<String, MetricProducerPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public MetricProducerModel withAdditionalProperty(
      String name, MetricProducerPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "MetricProducerModel{"
        + "opencensus="
        + opencensus
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.opencensus == null) ? 0 : this.opencensus.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MetricProducerModel) {
      MetricProducerModel that = (MetricProducerModel) o;
      return (this.opencensus == null
              ? that.opencensus == null
              : this.opencensus.equals(that.opencensus))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
