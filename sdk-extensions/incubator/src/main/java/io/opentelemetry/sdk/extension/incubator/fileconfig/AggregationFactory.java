/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Aggregation;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Base2ExponentialBucketHistogram;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogram;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class AggregationFactory
    implements Factory<Aggregation, io.opentelemetry.sdk.metrics.Aggregation> {

  private static final AggregationFactory INSTANCE = new AggregationFactory();

  private AggregationFactory() {}

  static AggregationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public io.opentelemetry.sdk.metrics.Aggregation create(
      @Nullable Aggregation model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      return io.opentelemetry.sdk.metrics.Aggregation.defaultAggregation();
    }

    if (model.getDrop() != null) {
      return io.opentelemetry.sdk.metrics.Aggregation.drop();
    }
    if (model.getSum() != null) {
      return io.opentelemetry.sdk.metrics.Aggregation.sum();
    }
    if (model.getLastValue() != null) {
      return io.opentelemetry.sdk.metrics.Aggregation.lastValue();
    }
    Base2ExponentialBucketHistogram exponentialBucketHistogram =
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
        return io.opentelemetry.sdk.metrics.Aggregation.base2ExponentialBucketHistogram(
            maxSize, maxScale);
      } catch (IllegalArgumentException e) {
        throw new StructuredConfigException("Invalid exponential bucket histogram", e);
      }
    }
    ExplicitBucketHistogram explicitBucketHistogram = model.getExplicitBucketHistogram();
    if (explicitBucketHistogram != null) {
      List<Double> boundaries = explicitBucketHistogram.getBoundaries();
      if (boundaries == null) {
        return io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram();
      }
      try {
        return io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram(boundaries);
      } catch (IllegalArgumentException e) {
        throw new StructuredConfigException("Invalid explicit bucket histogram", e);
      }
    }

    return io.opentelemetry.sdk.metrics.Aggregation.defaultAggregation();
  }
}
