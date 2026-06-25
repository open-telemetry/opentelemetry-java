/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"processors", "limits", "logger_configurator/development"})
@Generated("jsonschema2pojo")
public class LoggerProviderModel {

  /**
   * Configure log record processors. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("processors")
  @JsonPropertyDescription(
      "Configure log record processors.\nProperty is required and must be non-null.\n")
  @Nullable
  private List<LogRecordProcessorModel> processors;

  @JsonProperty("limits")
  @Nullable
  private LogRecordLimitsModel limits;

  @JsonProperty("logger_configurator/development")
  @Nullable
  private ExperimentalLoggerConfiguratorModel loggerConfiguratorDevelopment;

  /**
   * Configure log record processors. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("processors")
  @Nullable
  public List<LogRecordProcessorModel> getProcessors() {
    return processors;
  }

  public LoggerProviderModel withProcessors(List<LogRecordProcessorModel> processors) {
    this.processors = processors;
    return this;
  }

  @JsonProperty("limits")
  @Nullable
  public LogRecordLimitsModel getLimits() {
    return limits;
  }

  public LoggerProviderModel withLimits(LogRecordLimitsModel limits) {
    this.limits = limits;
    return this;
  }

  @JsonProperty("logger_configurator/development")
  @Nullable
  public ExperimentalLoggerConfiguratorModel getLoggerConfiguratorDevelopment() {
    return loggerConfiguratorDevelopment;
  }

  public LoggerProviderModel withLoggerConfiguratorDevelopment(
      ExperimentalLoggerConfiguratorModel loggerConfiguratorDevelopment) {
    this.loggerConfiguratorDevelopment = loggerConfiguratorDevelopment;
    return this;
  }

  @Override
  public String toString() {
    return "LoggerProviderModel{"
        + "processors="
        + processors
        + ", limits="
        + limits
        + ", loggerConfiguratorDevelopment="
        + loggerConfiguratorDevelopment
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.processors == null) ? 0 : this.processors.hashCode();
    h *= 1000003;
    h ^= (this.limits == null) ? 0 : this.limits.hashCode();
    h *= 1000003;
    h ^=
        (this.loggerConfiguratorDevelopment == null)
            ? 0
            : this.loggerConfiguratorDevelopment.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof LoggerProviderModel) {
      LoggerProviderModel that = (LoggerProviderModel) o;
      return (this.processors == null
              ? that.processors == null
              : this.processors.equals(that.processors))
          && (this.limits == null ? that.limits == null : this.limits.equals(that.limits))
          && (this.loggerConfiguratorDevelopment == null
              ? that.loggerConfiguratorDevelopment == null
              : this.loggerConfiguratorDevelopment.equals(that.loggerConfiguratorDevelopment));
    }
    return false;
  }
}
