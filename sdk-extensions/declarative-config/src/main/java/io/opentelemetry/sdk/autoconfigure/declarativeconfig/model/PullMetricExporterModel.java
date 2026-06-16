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
@JsonPropertyOrder({"prometheus/development"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class PullMetricExporterModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("prometheus/development")
  private ExperimentalPrometheusMetricExporterModel prometheusDevelopment;

  @JsonIgnore
  private Map<String, PullMetricExporterPropertyModel> additionalProperties =
      new LinkedHashMap<String, PullMetricExporterPropertyModel>();

  @JsonProperty("prometheus/development")
  @Nullable
  public ExperimentalPrometheusMetricExporterModel getPrometheusDevelopment() {
    return prometheusDevelopment;
  }

  public PullMetricExporterModel withPrometheusDevelopment(
      ExperimentalPrometheusMetricExporterModel prometheusDevelopment) {
    this.prometheusDevelopment = prometheusDevelopment;
    return this;
  }

  @JsonAnyGetter
  public Map<String, PullMetricExporterPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, PullMetricExporterPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public PullMetricExporterModel withAdditionalProperty(
      String name, PullMetricExporterPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(PullMetricExporterModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("prometheusDevelopment");
    sb.append('=');
    sb.append(((this.prometheusDevelopment == null) ? "<null>" : this.prometheusDevelopment));
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
    result =
        ((result * 31)
            + ((this.prometheusDevelopment == null) ? 0 : this.prometheusDevelopment.hashCode()));
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
    if ((other instanceof PullMetricExporterModel) == false) {
      return false;
    }
    PullMetricExporterModel rhs = ((PullMetricExporterModel) other);
    return (((this.prometheusDevelopment == rhs.prometheusDevelopment)
            || ((this.prometheusDevelopment != null)
                && this.prometheusDevelopment.equals(rhs.prometheusDevelopment)))
        && ((this.additionalProperties == rhs.additionalProperties)
            || ((this.additionalProperties != null)
                && this.additionalProperties.equals(rhs.additionalProperties))));
  }
}
