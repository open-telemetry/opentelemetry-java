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
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"enabled", "minimum_severity", "trace_based"})
@Generated("jsonschema2pojo")
public class ExperimentalLoggerConfigModel {

  /** Configure if the logger is enabled or not. If omitted or null, true is used. */
  @JsonProperty("enabled")
  @JsonPropertyDescription(
      "Configure if the logger is enabled or not.\nIf omitted or null, true is used.\n")
  @Nullable
  private Boolean enabled;

  @JsonProperty("minimum_severity")
  @Nullable
  private OpenTelemetryConfigurationModel.SeverityNumber minimumSeverity;

  /**
   * Configure trace based filtering. If true, log records associated with unsampled trace contexts
   * traces are not processed. If false, or if a log record is not associated with a trace context,
   * trace based filtering is not applied. If omitted or null, trace based filtering is not applied.
   */
  @JsonProperty("trace_based")
  @JsonPropertyDescription(
      "Configure trace based filtering.\nIf true, log records associated with unsampled trace contexts traces are not processed. If false, or if a log record is not associated with a trace context, trace based filtering is not applied.\nIf omitted or null, trace based filtering is not applied.\n")
  @Nullable
  private Boolean traceBased;

  /** Configure if the logger is enabled or not. If omitted or null, true is used. */
  @JsonProperty("enabled")
  @Nullable
  public Boolean getEnabled() {
    return enabled;
  }

  public ExperimentalLoggerConfigModel withEnabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @JsonProperty("minimum_severity")
  @Nullable
  public OpenTelemetryConfigurationModel.SeverityNumber getMinimumSeverity() {
    return minimumSeverity;
  }

  public ExperimentalLoggerConfigModel withMinimumSeverity(
      OpenTelemetryConfigurationModel.SeverityNumber minimumSeverity) {
    this.minimumSeverity = minimumSeverity;
    return this;
  }

  /**
   * Configure trace based filtering. If true, log records associated with unsampled trace contexts
   * traces are not processed. If false, or if a log record is not associated with a trace context,
   * trace based filtering is not applied. If omitted or null, trace based filtering is not applied.
   */
  @JsonProperty("trace_based")
  @Nullable
  public Boolean getTraceBased() {
    return traceBased;
  }

  public ExperimentalLoggerConfigModel withTraceBased(Boolean traceBased) {
    this.traceBased = traceBased;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalLoggerConfigModel{"
        + "enabled="
        + enabled
        + ", minimumSeverity="
        + minimumSeverity
        + ", traceBased="
        + traceBased
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.enabled == null) ? 0 : this.enabled.hashCode();
    h *= 1000003;
    h ^= (this.minimumSeverity == null) ? 0 : this.minimumSeverity.hashCode();
    h *= 1000003;
    h ^= (this.traceBased == null) ? 0 : this.traceBased.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalLoggerConfigModel) {
      ExperimentalLoggerConfigModel that = (ExperimentalLoggerConfigModel) o;
      return (this.enabled == null ? that.enabled == null : this.enabled.equals(that.enabled))
          && (this.minimumSeverity == null
              ? that.minimumSeverity == null
              : this.minimumSeverity.equals(that.minimumSeverity))
          && (this.traceBased == null
              ? that.traceBased == null
              : this.traceBased.equals(that.traceBased));
    }
    return false;
  }
}
