/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SeverityNumberModel;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"enabled", "minimum_severity", "trace_based"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalLoggerConfigModel {

  @Nullable private Boolean enabled;
  @Nullable private SeverityNumberModel minimumSeverity;
  @Nullable private Boolean traceBased;

  /**
   * Configure if the logger is enabled or not.
   *
   * <p>If omitted or null, true is used.
   */
  @JsonProperty("enabled")
  @Nullable
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty("enabled")
  public ExperimentalLoggerConfigModel withEnabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * Configure severity filtering.
   *
   * <p>Log records with an non-zero (i.e. unspecified) severity number which is less than
   * minimum_severity are not processed.
   *
   * <p>Values include:
   *
   * <p>* debug: debug, severity number 5.
   *
   * <p>* debug2: debug2, severity number 6.
   *
   * <p>* debug3: debug3, severity number 7.
   *
   * <p>* debug4: debug4, severity number 8.
   *
   * <p>* error: error, severity number 17.
   *
   * <p>* error2: error2, severity number 18.
   *
   * <p>* error3: error3, severity number 19.
   *
   * <p>* error4: error4, severity number 20.
   *
   * <p>* fatal: fatal, severity number 21.
   *
   * <p>* fatal2: fatal2, severity number 22.
   *
   * <p>* fatal3: fatal3, severity number 23.
   *
   * <p>* fatal4: fatal4, severity number 24.
   *
   * <p>* info: info, severity number 9.
   *
   * <p>* info2: info2, severity number 10.
   *
   * <p>* info3: info3, severity number 11.
   *
   * <p>* info4: info4, severity number 12.
   *
   * <p>* trace: trace, severity number 1.
   *
   * <p>* trace2: trace2, severity number 2.
   *
   * <p>* trace3: trace3, severity number 3.
   *
   * <p>* trace4: trace4, severity number 4.
   *
   * <p>* warn: warn, severity number 13.
   *
   * <p>* warn2: warn2, severity number 14.
   *
   * <p>* warn3: warn3, severity number 15.
   *
   * <p>* warn4: warn4, severity number 16.
   *
   * <p>If omitted, severity filtering is not applied.
   */
  @JsonProperty("minimum_severity")
  @Nullable
  public SeverityNumberModel getMinimumSeverity() {
    return minimumSeverity;
  }

  @JsonProperty("minimum_severity")
  public ExperimentalLoggerConfigModel withMinimumSeverity(SeverityNumberModel minimumSeverity) {
    this.minimumSeverity = minimumSeverity;
    return this;
  }

  /**
   * Configure trace based filtering.
   *
   * <p>If true, log records associated with unsampled trace contexts traces are not processed. If
   * false, or if a log record is not associated with a trace context, trace based filtering is not
   * applied.
   *
   * <p>If omitted or null, trace based filtering is not applied.
   */
  @JsonProperty("trace_based")
  @Nullable
  public Boolean getTraceBased() {
    return traceBased;
  }

  @JsonProperty("trace_based")
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
