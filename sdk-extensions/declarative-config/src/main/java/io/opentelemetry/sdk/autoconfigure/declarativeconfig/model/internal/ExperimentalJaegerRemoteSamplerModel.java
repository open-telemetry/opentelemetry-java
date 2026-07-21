/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"endpoint", "interval", "initial_sampler"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalJaegerRemoteSamplerModel {

  @Nullable private String endpoint;
  @Nullable private Integer interval;
  @Nullable private SamplerModel initialSampler;

  /**
   * Configure the endpoint of the jaeger remote sampling service.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("endpoint")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  @JsonProperty("endpoint")
  public ExperimentalJaegerRemoteSamplerModel withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Configure the polling interval (in milliseconds) to fetch from the remote sampling service.
   *
   * <p>If omitted or null, 60000 is used.
   */
  @JsonProperty("interval")
  @Nullable
  public Integer getInterval() {
    return interval;
  }

  @JsonProperty("interval")
  public ExperimentalJaegerRemoteSamplerModel withInterval(Integer interval) {
    this.interval = interval;
    return this;
  }

  /**
   * Configure the initial sampler used before first configuration is fetched.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("initial_sampler")
  @Nullable
  public SamplerModel getInitialSampler() {
    return initialSampler;
  }

  @JsonProperty("initial_sampler")
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
