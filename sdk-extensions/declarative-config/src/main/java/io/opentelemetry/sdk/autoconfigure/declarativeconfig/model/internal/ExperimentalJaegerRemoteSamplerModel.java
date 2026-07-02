/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"endpoint", "interval", "initial_sampler"})
@Generated("jsonschema2pojo")
public class ExperimentalJaegerRemoteSamplerModel {

  /**
   * Configure the endpoint of the jaeger remote sampling service. Property is required and must be
   * non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("endpoint")
  @JsonPropertyDescription(
      "Configure the endpoint of the jaeger remote sampling service.\nProperty is required and must be non-null.\n")
  @Nullable
  private String endpoint;

  /**
   * Configure the polling interval (in milliseconds) to fetch from the remote sampling service. If
   * omitted or null, 60000 is used.
   */
  @JsonProperty("interval")
  @JsonPropertyDescription(
      "Configure the polling interval (in milliseconds) to fetch from the remote sampling service.\nIf omitted or null, 60000 is used.\n")
  @Nullable
  private Integer interval;

  /** (Required) */
  @JsonProperty("initial_sampler")
  @Nullable
  private SamplerModel initialSampler;

  /**
   * Configure the endpoint of the jaeger remote sampling service. Property is required and must be
   * non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("endpoint")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  public ExperimentalJaegerRemoteSamplerModel withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Configure the polling interval (in milliseconds) to fetch from the remote sampling service. If
   * omitted or null, 60000 is used.
   */
  @JsonProperty("interval")
  @Nullable
  public Integer getInterval() {
    return interval;
  }

  public ExperimentalJaegerRemoteSamplerModel withInterval(Integer interval) {
    this.interval = interval;
    return this;
  }

  /** (Required) */
  @JsonProperty("initial_sampler")
  @Nullable
  public SamplerModel getInitialSampler() {
    return initialSampler;
  }

  public ExperimentalJaegerRemoteSamplerModel withInitialSampler(SamplerModel initialSampler) {
    this.initialSampler = initialSampler;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalJaegerRemoteSamplerModel{"
        + "endpoint="
        + endpoint
        + ", interval="
        + interval
        + ", initialSampler="
        + initialSampler
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.endpoint == null) ? 0 : this.endpoint.hashCode();
    h *= 1000003;
    h ^= (this.interval == null) ? 0 : this.interval.hashCode();
    h *= 1000003;
    h ^= (this.initialSampler == null) ? 0 : this.initialSampler.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalJaegerRemoteSamplerModel) {
      ExperimentalJaegerRemoteSamplerModel that = (ExperimentalJaegerRemoteSamplerModel) o;
      return (this.endpoint == null ? that.endpoint == null : this.endpoint.equals(that.endpoint))
          && (this.interval == null ? that.interval == null : this.interval.equals(that.interval))
          && (this.initialSampler == null
              ? that.initialSampler == null
              : this.initialSampler.equals(that.initialSampler));
    }
    return false;
  }
}
