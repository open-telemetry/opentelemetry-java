/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"periodic", "pull"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class MetricReaderModel {

  @Nullable
  @JsonProperty("periodic")
  private PeriodicMetricReaderModel periodic;

  @Nullable
  @JsonProperty("pull")
  private PullMetricReaderModel pull;

  @JsonProperty("periodic")
  @Nullable
  public PeriodicMetricReaderModel getPeriodic() {
    return periodic;
  }

  public MetricReaderModel withPeriodic(PeriodicMetricReaderModel periodic) {
    this.periodic = periodic;
    return this;
  }

  @JsonProperty("pull")
  @Nullable
  public PullMetricReaderModel getPull() {
    return pull;
  }

  public MetricReaderModel withPull(PullMetricReaderModel pull) {
    this.pull = pull;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(MetricReaderModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("periodic");
    sb.append('=');
    sb.append(((this.periodic == null) ? "<null>" : this.periodic));
    sb.append(',');
    sb.append("pull");
    sb.append('=');
    sb.append(((this.pull == null) ? "<null>" : this.pull));
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
    result = ((result * 31) + ((this.pull == null) ? 0 : this.pull.hashCode()));
    result = ((result * 31) + ((this.periodic == null) ? 0 : this.periodic.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof MetricReaderModel) == false) {
      return false;
    }
    MetricReaderModel rhs = ((MetricReaderModel) other);
    return (((this.pull == rhs.pull) || ((this.pull != null) && this.pull.equals(rhs.pull)))
        && ((this.periodic == rhs.periodic)
            || ((this.periodic != null) && this.periodic.equals(rhs.periodic))));
  }
}
