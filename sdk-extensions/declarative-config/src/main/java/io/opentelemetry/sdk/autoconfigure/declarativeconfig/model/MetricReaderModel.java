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
public class MetricReaderModel {

  @Nullable private PeriodicMetricReaderModel periodic;
  @Nullable private PullMetricReaderModel pull;

  /**
   * Configure a periodic metric reader.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("periodic")
  @Nullable
  public PeriodicMetricReaderModel getPeriodic() {
    return periodic;
  }

  @JsonProperty("periodic")
  public MetricReaderModel withPeriodic(PeriodicMetricReaderModel periodic) {
    this.periodic = periodic;
    return this;
  }

  /**
   * Configure a pull based metric reader.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("pull")
  @Nullable
  public PullMetricReaderModel getPull() {
    return pull;
  }

  @JsonProperty("pull")
  public MetricReaderModel withPull(PullMetricReaderModel pull) {
    this.pull = pull;
    return this;
  }

  @Override
  public String toString() {
    return "MetricReaderModel{" + "periodic=" + periodic + ", pull=" + pull + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.periodic == null) ? 0 : this.periodic.hashCode();
    h *= 1000003;
    h ^= (this.pull == null) ? 0 : this.pull.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MetricReaderModel) {
      MetricReaderModel that = (MetricReaderModel) o;
      return (this.periodic == null ? that.periodic == null : this.periodic.equals(that.periodic))
          && (this.pull == null ? that.pull == null : this.pull.equals(that.pull));
    }
    return false;
  }
}
