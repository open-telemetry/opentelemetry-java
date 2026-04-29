/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import javax.annotation.Nullable;

/** Builder for {@link PrometheusMetricReader}. */
public final class PrometheusMetricReaderBuilder {

  private boolean otelScopeLabelsEnabled = true;
  private boolean targetInfoMetricEnabled = true;
  private TranslationStrategy translationStrategy =
      TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES;
  @Nullable private Predicate<String> allowedResourceAttributesFilter;

  PrometheusMetricReaderBuilder() {}

  PrometheusMetricReaderBuilder(PrometheusMetricReaderBuilder metricReaderBuilder) {
    this.otelScopeLabelsEnabled = metricReaderBuilder.otelScopeLabelsEnabled;
    this.targetInfoMetricEnabled = metricReaderBuilder.targetInfoMetricEnabled;
    this.translationStrategy = metricReaderBuilder.translationStrategy;
    this.allowedResourceAttributesFilter = metricReaderBuilder.allowedResourceAttributesFilter;
  }

  /**
   * Sets whether to add OpenTelemetry scope labels (otel_scope_name, otel_scope_version, etc.) to
   * exported metrics. Default is {@code true}.
   *
   * @param otelScopeLabelsEnabled whether to add scope labels
   * @return this builder
   */
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
  public PrometheusMetricReaderBuilder setTargetInfoMetricEnabled(boolean targetInfoMetricEnabled) {
    this.targetInfoMetricEnabled = targetInfoMetricEnabled;
    return this;
  }

  /**
   * Sets the translation strategy for metric and label name conversion.
   *
   * @param translationStrategy the strategy to use
   * @return this builder
   * @see TranslationStrategy
   */
  public PrometheusMetricReaderBuilder setTranslationStrategy(
      TranslationStrategy translationStrategy) {
    requireNonNull(translationStrategy, "translationStrategy");
    this.translationStrategy = translationStrategy;
    return this;
  }

  /**
   * Sets a filter to control which resource attributes are added as labels on each exported metric.
   * If {@code null}, no resource attributes will be added as labels. Default is {@code null}.
   *
   * @param allowedResourceAttributesFilter predicate to filter resource attributes, or {@code null}
   * @return this builder
   */
  public PrometheusMetricReaderBuilder setAllowedResourceAttributesFilter(
      @Nullable Predicate<String> allowedResourceAttributesFilter) {
    this.allowedResourceAttributesFilter = allowedResourceAttributesFilter;
    return this;
  }

  TranslationStrategy getTranslationStrategy() {
    return translationStrategy;
  }

  /** Builds a new {@link PrometheusMetricReader}. */
  public PrometheusMetricReader build() {
    return new PrometheusMetricReader(
        allowedResourceAttributesFilter,
        otelScopeLabelsEnabled,
        targetInfoMetricEnabled,
        translationStrategy);
  }
}
