/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** Builder for {@link PrometheusMetricReader}. */
public final class PrometheusMetricReaderBuilder {

  private boolean otelScopeLabelsEnabled = true;
  private boolean targetInfoMetricEnabled = true;
  @Nullable private Predicate<String> allowedResourceAttributesFilter;

  PrometheusMetricReaderBuilder() {}

  /**
   * Sets whether to add OpenTelemetry scope labels (otel_scope_name, otel_scope_version, etc.) to
   * exported metrics. Default is {@code true}.
   *
   * @param otelScopeLabelsEnabled whether to add scope labels
   * @return this builder
   */
  @CanIgnoreReturnValue
  public PrometheusMetricReaderBuilder setOtelScopeLabelsEnabled(boolean otelScopeLabelsEnabled) {
    this.otelScopeLabelsEnabled = otelScopeLabelsEnabled;
    return this;
  }

  /**
   * Sets whether to export the target_info metric with resource attributes. Default is {@code
   * true}.
   *
   * @param targetInfoMetricEnabled whether to export target_info metric
   * @return this builder
   */
  @CanIgnoreReturnValue
  public PrometheusMetricReaderBuilder setTargetInfoMetricEnabled(
      boolean targetInfoMetricEnabled) {
    this.targetInfoMetricEnabled = targetInfoMetricEnabled;
    return this;
  }

  /**
   * Sets a filter to control which resource attributes are added as labels on each exported metric.
   * If {@code null}, no resource attributes will be added as labels. Default is {@code null}.
   *
   * @param allowedResourceAttributesFilter predicate to filter resource attributes, or {@code null}
   * @return this builder
   */
  @CanIgnoreReturnValue
  public PrometheusMetricReaderBuilder setAllowedResourceAttributesFilter(
      @Nullable Predicate<String> allowedResourceAttributesFilter) {
    this.allowedResourceAttributesFilter = allowedResourceAttributesFilter;
    return this;
  }

  /** Builds a new {@link PrometheusMetricReader}. */
  public PrometheusMetricReader build() {
    return new PrometheusMetricReader(
        allowedResourceAttributesFilter, otelScopeLabelsEnabled, targetInfoMetricEnabled);
  }
}
