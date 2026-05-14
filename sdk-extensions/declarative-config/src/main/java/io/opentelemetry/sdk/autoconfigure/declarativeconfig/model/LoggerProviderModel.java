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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"processors", "limits", "logger_configurator/development"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class LoggerProviderModel {

  /**
   * Configure log record processors. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("processors")
  @JsonPropertyDescription(
      "Configure log record processors.\nProperty is required and must be non-null.\n")
  @Nonnull
  private List<LogRecordProcessorModel> processors;

  /** (Can be null) */
  @Nullable
  @JsonProperty("limits")
  private LogRecordLimitsModel limits;

  /** (Can be null) */
  @Nullable
  @JsonProperty("logger_configurator/development")
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
    StringBuilder sb = new StringBuilder();
    sb.append(LoggerProviderModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("processors");
    sb.append('=');
    sb.append(((this.processors == null) ? "<null>" : this.processors));
    sb.append(',');
    sb.append("limits");
    sb.append('=');
    sb.append(((this.limits == null) ? "<null>" : this.limits));
    sb.append(',');
    sb.append("loggerConfiguratorDevelopment");
    sb.append('=');
    sb.append(
        ((this.loggerConfiguratorDevelopment == null)
            ? "<null>"
            : this.loggerConfiguratorDevelopment));
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
    result = ((result * 31) + ((this.limits == null) ? 0 : this.limits.hashCode()));
    result = ((result * 31) + ((this.processors == null) ? 0 : this.processors.hashCode()));
    result =
        ((result * 31)
            + ((this.loggerConfiguratorDevelopment == null)
                ? 0
                : this.loggerConfiguratorDevelopment.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof LoggerProviderModel) == false) {
      return false;
    }
    LoggerProviderModel rhs = ((LoggerProviderModel) other);
    return ((((this.limits == rhs.limits)
                || ((this.limits != null) && this.limits.equals(rhs.limits)))
            && ((this.processors == rhs.processors)
                || ((this.processors != null) && this.processors.equals(rhs.processors))))
        && ((this.loggerConfiguratorDevelopment == rhs.loggerConfiguratorDevelopment)
            || ((this.loggerConfiguratorDevelopment != null)
                && this.loggerConfiguratorDevelopment.equals(rhs.loggerConfiguratorDevelopment))));
  }
}
