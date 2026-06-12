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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalLoggerConfigModel {

  /**
   * Configure if the logger is enabled or not. If omitted or null, true is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("enabled")
  @JsonPropertyDescription(
      "Configure if the logger is enabled or not.\nIf omitted or null, true is used.\n")
  private Boolean enabled;

  /** (Can be null) */
  @Nullable
  @JsonProperty("minimum_severity")
  private OpenTelemetryConfigurationModel.SeverityNumber minimumSeverity;

  /**
   * Configure trace based filtering. If true, log records associated with unsampled trace contexts
   * traces are not processed. If false, or if a log record is not associated with a trace context,
   * trace based filtering is not applied. If omitted or null, trace based filtering is not applied.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("trace_based")
  @JsonPropertyDescription(
      "Configure trace based filtering.\nIf true, log records associated with unsampled trace contexts traces are not processed. If false, or if a log record is not associated with a trace context, trace based filtering is not applied.\nIf omitted or null, trace based filtering is not applied.\n")
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalLoggerConfigModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("enabled");
    sb.append('=');
    sb.append(((this.enabled == null) ? "<null>" : this.enabled));
    sb.append(',');
    sb.append("minimumSeverity");
    sb.append('=');
    sb.append(((this.minimumSeverity == null) ? "<null>" : this.minimumSeverity));
    sb.append(',');
    sb.append("traceBased");
    sb.append('=');
    sb.append(((this.traceBased == null) ? "<null>" : this.traceBased));
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
    result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
    result =
        ((result * 31) + ((this.minimumSeverity == null) ? 0 : this.minimumSeverity.hashCode()));
    result = ((result * 31) + ((this.traceBased == null) ? 0 : this.traceBased.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalLoggerConfigModel) == false) {
      return false;
    }
    ExperimentalLoggerConfigModel rhs = ((ExperimentalLoggerConfigModel) other);
    return ((((this.enabled == rhs.enabled)
                || ((this.enabled != null) && this.enabled.equals(rhs.enabled)))
            && ((this.minimumSeverity == rhs.minimumSeverity)
                || ((this.minimumSeverity != null)
                    && this.minimumSeverity.equals(rhs.minimumSeverity))))
        && ((this.traceBased == rhs.traceBased)
            || ((this.traceBased != null) && this.traceBased.equals(rhs.traceBased))));
  }
}
