/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class MetricProducerModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("opencensus")
  private OpenCensusMetricProducerModel opencensus;

  @JsonIgnore
  private Map<String, MetricProducerPropertyModel> additionalProperties =
      new LinkedHashMap<String, MetricProducerPropertyModel>();

  @JsonProperty("opencensus")
  @Nullable
  public OpenCensusMetricProducerModel getOpencensus() {
    return opencensus;
  }

  public MetricProducerModel withOpencensus(OpenCensusMetricProducerModel opencensus) {
    this.opencensus = opencensus;
    return this;
  }

  @JsonAnyGetter
  public Map<String, MetricProducerPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, MetricProducerPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public MetricProducerModel withAdditionalProperty(
      String name, MetricProducerPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(MetricProducerModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("opencensus");
    sb.append('=');
    sb.append(((this.opencensus == null) ? "<null>" : this.opencensus));
    sb.append(',');
    sb.append("additionalProperties");
    sb.append('=');
    sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.opencensus == null) ? 0 : this.opencensus.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof MetricProducerModel) == false) {
      return false;
    }
    MetricProducerModel rhs = ((MetricProducerModel) other);
    return (((this.opencensus == rhs.opencensus)
            || ((this.opencensus != null) && this.opencensus.equals(rhs.opencensus)))
        && ((this.additionalProperties == rhs.additionalProperties)
            || ((this.additionalProperties != null)
                && this.additionalProperties.equals(rhs.additionalProperties))));
  }
}
