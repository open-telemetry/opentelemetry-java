/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"endpoint", "interval", "initial_sampler"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
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
  @Nonnull
  private String endpoint;

  /**
   * Configure the polling interval (in milliseconds) to fetch from the remote sampling service. If
   * omitted or null, 60000 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("interval")
  @JsonPropertyDescription(
      "Configure the polling interval (in milliseconds) to fetch from the remote sampling service.\nIf omitted or null, 60000 is used.\n")
  private Integer interval;

  /** (Required) */
  @JsonProperty("initial_sampler")
  @Nonnull
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalJaegerRemoteSamplerModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("endpoint");
    sb.append('=');
    sb.append(((this.endpoint == null) ? "<null>" : this.endpoint));
    sb.append(',');
    sb.append("interval");
    sb.append('=');
    sb.append(((this.interval == null) ? "<null>" : this.interval));
    sb.append(',');
    sb.append("initialSampler");
    sb.append('=');
    sb.append(((this.initialSampler == null) ? "<null>" : this.initialSampler));
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
    result = ((result * 31) + ((this.endpoint == null) ? 0 : this.endpoint.hashCode()));
    result = ((result * 31) + ((this.interval == null) ? 0 : this.interval.hashCode()));
    result = ((result * 31) + ((this.initialSampler == null) ? 0 : this.initialSampler.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalJaegerRemoteSamplerModel) == false) {
      return false;
    }
    ExperimentalJaegerRemoteSamplerModel rhs = ((ExperimentalJaegerRemoteSamplerModel) other);
    return ((((this.endpoint == rhs.endpoint)
                || ((this.endpoint != null) && this.endpoint.equals(rhs.endpoint)))
            && ((this.interval == rhs.interval)
                || ((this.interval != null) && this.interval.equals(rhs.interval))))
        && ((this.initialSampler == rhs.initialSampler)
            || ((this.initialSampler != null) && this.initialSampler.equals(rhs.initialSampler))));
  }
}
