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
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalPrometheusMetricExporterModel;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"prometheus/development"})
@Generated("jsonschema2pojo")
public class PullMetricExporterModel {

  @Nullable private ExperimentalPrometheusMetricExporterModel prometheusDevelopment;
  private Map<String, PullMetricExporterPropertyModel> additionalProperties =
      new LinkedHashMap<String, PullMetricExporterPropertyModel>();

  /**
   * Configure exporter to be prometheus.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("prometheus/development")
  @Nullable
  public ExperimentalPrometheusMetricExporterModel getPrometheusDevelopment() {
    return prometheusDevelopment;
  }

  @JsonProperty("prometheus/development")
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
  public PullMetricExporterModel withAdditionalProperty(
      String name, PullMetricExporterPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "PullMetricExporterModel{"
        + "prometheusDevelopment="
        + prometheusDevelopment
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.prometheusDevelopment == null) ? 0 : this.prometheusDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PullMetricExporterModel) {
      PullMetricExporterModel that = (PullMetricExporterModel) o;
      return (this.prometheusDevelopment == null
              ? that.prometheusDevelopment == null
              : this.prometheusDevelopment.equals(that.prometheusDevelopment))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
