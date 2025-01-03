/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Base2ExponentialBucketHistogramModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogramModel;
import io.opentelemetry.sdk.metrics.Aggregation;
import java.io.Closeable;
import java.util.List;

final class AggregationFactory implements Factory<AggregationModel, Aggregation> {

  private static final AggregationFactory INSTANCE = new AggregationFactory();

  private AggregationFactory() {}

  static AggregationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Aggregation create(
      AggregationModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model.getDrop() != null) {
      return Aggregation.drop();
    }
    if (model.getSum() != null) {
      return Aggregation.sum();
    }
    if (model.getLastValue() != null) {
      return Aggregation.lastValue();
    }
    Base2ExponentialBucketHistogramModel exponentialBucketHistogram =
        model.getBase2ExponentialBucketHistogram();
    if (exponentialBucketHistogram != null) {
      Integer maxScale = exponentialBucketHistogram.getMaxScale();
      if (maxScale == null) {
        maxScale = 20;
      }
      Integer maxSize = exponentialBucketHistogram.getMaxSize();
      if (maxSize == null) {
        maxSize = 160;
      }
      try {
        return Aggregation.base2ExponentialBucketHistogram(maxSize, maxScale);
      } catch (IllegalArgumentException e) {
        throw new ConfigurationException("Invalid exponential bucket histogram", e);
      }
    }
    ExplicitBucketHistogramModel explicitBucketHistogram = model.getExplicitBucketHistogram();
    if (explicitBucketHistogram != null) {
      List<Double> boundaries = explicitBucketHistogram.getBoundaries();
      if (boundaries == null) {
        return Aggregation.explicitBucketHistogram();
      }
      try {
        return Aggregation.explicitBucketHistogram(boundaries);
      } catch (IllegalArgumentException e) {
        throw new ConfigurationException("Invalid explicit bucket histogram", e);
      }
    }

    return Aggregation.defaultAggregation();
  }
}
