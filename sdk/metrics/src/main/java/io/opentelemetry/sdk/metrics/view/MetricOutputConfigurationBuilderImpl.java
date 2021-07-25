/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.HistogramConfig;
import io.opentelemetry.sdk.metrics.aggregator.LastValueConfig;
import io.opentelemetry.sdk.metrics.aggregator.SumConfig;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;

/** Actual implementation that builds views from the API. */
class MetricOutputConfigurationBuilderImpl implements MetricOutputConfigurationBuilder {
  enum Aggregator {
    LAST_VALUE,
    SUM,
    EXPLICIT_BUCKET_HISTOGRAM
  }

  private AttributesProcessor currentProcessor = AttributesProcessor.NOOP;
  private Aggregator aggregation = Aggregator.LAST_VALUE;
  private AggregationTemporality temporality = AggregationTemporality.CUMULATIVE;
  private double[] histogramBoundaries = HistogramConfig.DEFAULT_HISTOGRAM_BOUNDARIES;
  private String viewName = null;
  private String viewDescription = null;

  @Override
  public MetricOutputConfigurationBuilder setName(String name) {
    this.viewName = name;
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder setDescription(String description) {
    this.viewDescription = description;
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder setAttributeKeyFilter(AttributeKey<?>... keys) {
    AttributesProcessor filter = AttributesProcessors.filterKeys(keys);
    this.currentProcessor = currentProcessor.then(filter);
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder setExtraDimensions(String... keys) {
    AttributesProcessor append = AttributesProcessors.appendBaggageByKeys(keys);
    this.currentProcessor = append.then(this.currentProcessor);
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder aggregateAsSum() {
    this.aggregation = Aggregator.SUM;
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder aggregateAsLastValue() {
    this.aggregation = Aggregator.LAST_VALUE;
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder aggregateAsHistogram() {
    this.aggregation = Aggregator.EXPLICIT_BUCKET_HISTOGRAM;
    this.histogramBoundaries = HistogramConfig.DEFAULT_HISTOGRAM_BOUNDARIES;
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder withDeltaAggregation() {
    temporality = AggregationTemporality.DELTA;
    return this;
  }

  @Override
  public MetricOutputConfigurationBuilder aggregateAsHistogramWithFixedBoundaries(
      double[] boundaries) {
    this.aggregation = Aggregator.EXPLICIT_BUCKET_HISTOGRAM;
    this.histogramBoundaries = boundaries;
    return this;
  }

  @Override
  public MetricOutputConfiguration build() {
    return new MetricOutputConfigurationImpl(
        currentProcessor, aggregation, temporality, histogramBoundaries, viewName, viewDescription);
  }

  static class MetricOutputConfigurationImpl implements MetricOutputConfiguration {
    private final AttributesProcessor currentProcessor;
    private final Aggregator aggregation;
    private final AggregationTemporality temporality;
    private final double[] histogramBoundaries;
    private final String viewName;
    private final String viewDescription;

    MetricOutputConfigurationImpl(
        AttributesProcessor currentProcessor,
        Aggregator aggregation,
        AggregationTemporality temporality,
        double[] histogramBoundaries,
        String viewName,
        String viewDescription) {
      this.currentProcessor = currentProcessor;
      this.aggregation = aggregation;
      this.temporality = temporality;
      this.histogramBoundaries = histogramBoundaries;
      this.viewName = viewName;
      this.viewDescription = viewDescription;
    }

    @Override
    public String getName() {
      return viewName;
    }

    @Override
    public AggregatorFactory<?> getAggregator(InstrumentDescriptor instrument) {
      switch (aggregation) {
        case SUM:
          return sum(instrument);
        case EXPLICIT_BUCKET_HISTOGRAM:
          return histogram(instrument);
        case LAST_VALUE:
          return lastValueGauge(instrument);
      }
      throw new IllegalStateException("View configured without known aggregation type!");
    }

    private AggregatorFactory<?> lastValueGauge(InstrumentDescriptor instrument) {
      return AggregatorFactory.lastValue(
          LastValueConfig.builder()
              .setName(viewName != null ? viewName : instrument.getName())
              .setDescription(
                  viewDescription != null ? viewDescription : instrument.getDescription())
              .setUnit(instrument.getUnit())
              .build());
    }

    private AggregatorFactory<?> sum(InstrumentDescriptor instrument) {
      boolean isMonotonic = false;
      switch (instrument.getType()) {
        case COUNTER:
        case HISTOGRAM:
        case OBSERVABLE_SUM:
          isMonotonic = true;
          break;
        default:
          break;
      }
      return AggregatorFactory.doubleSum(
          SumConfig.builder()
              .setName(viewName != null ? viewName : instrument.getName())
              .setDescription(
                  viewDescription != null ? viewDescription : instrument.getDescription())
              .setUnit(instrument.getUnit())
              .setMonotonic(isMonotonic)
              .setTemporality(temporality)
              .setMeasurementTemporality(
                  instrument.getType().isSynchronous()
                      ? AggregationTemporality.DELTA
                      : AggregationTemporality.CUMULATIVE)
              .build());
    }

    private AggregatorFactory<?> histogram(InstrumentDescriptor instrument) {
      return AggregatorFactory.doubleHistogram(
          HistogramConfig.builder()
              .setName(viewName != null ? viewName : instrument.getName())
              .setDescription(
                  viewDescription != null ? viewDescription : instrument.getDescription())
              .setUnit(instrument.getUnit())
              .setTemporality(temporality)
              .setBoundaries(histogramBoundaries)
              .build());
    }

    @Override
    public AttributesProcessor getAttributesProcessor() {
      return currentProcessor;
    }
  }
}
