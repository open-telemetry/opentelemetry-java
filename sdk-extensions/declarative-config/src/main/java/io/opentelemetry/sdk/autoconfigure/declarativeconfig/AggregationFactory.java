/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.Base2ExponentialBucketHistogramAggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.Base2ExponentialHistogramOptions;
import io.opentelemetry.sdk.metrics.ExplicitBucketHistogramOptions;
import java.util.List;

final class AggregationFactory implements Factory<AggregationModel, Aggregation> {

  private static final AggregationFactory INSTANCE = new AggregationFactory();

  private AggregationFactory() {}

  static AggregationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Aggregation create(AggregationModel model, DeclarativeConfigContext context) {
    if (model.getDrop() != null) {
      return Aggregation.drop();
    }
    if (model.getSum() != null) {
      return Aggregation.sum();
    }
    if (model.getLastValue() != null) {
      return Aggregation.lastValue();
    }
    Base2ExponentialBucketHistogramAggregationModel exponentialBucketHistogram =
        model.getBase2ExponentialBucketHistogram();
    if (exponentialBucketHistogram != null) {
      Integer maxScale = exponentialBucketHistogram.getMaxScale();
      Integer maxSize = exponentialBucketHistogram.getMaxSize();
      Boolean recordMinMax = exponentialBucketHistogram.getRecordMinMax();
      Base2ExponentialHistogramOptions.Builder builder = Base2ExponentialHistogramOptions.builder();
      if (maxScale != null) {
        builder.setMaxScale(maxScale);
      }
      if (maxSize != null) {
        builder.setMaxBuckets(maxSize);
      }
      if (recordMinMax != null) {
        builder.setRecordMinMax(recordMinMax);
      }
      try {
        return Aggregation.base2ExponentialBucketHistogram(builder.build());
      } catch (IllegalArgumentException e) {
        throw new DeclarativeConfigException("Invalid exponential bucket histogram", e);
      }
    }
    ExplicitBucketHistogramAggregationModel explicitBucketHistogram =
        model.getExplicitBucketHistogram();
    if (explicitBucketHistogram != null) {
      List<Double> boundaries = explicitBucketHistogram.getBoundaries();
      Boolean recordMinMax = explicitBucketHistogram.getRecordMinMax();
      ExplicitBucketHistogramOptions.Builder builder = ExplicitBucketHistogramOptions.builder();
      if (boundaries != null) {
        builder.setBucketBoundaries(boundaries);
      }
      if (recordMinMax != null) {
        builder.setRecordMinMax(recordMinMax);
      }
      try {
        return Aggregation.explicitBucketHistogram(builder.build());
      } catch (IllegalArgumentException e) {
        throw new DeclarativeConfigException("Invalid explicit bucket histogram", e);
      }
    }

    return Aggregation.defaultAggregation();
  }
}
