/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Base2ExponentialBucketHistogramAggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.HistogramOptions;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
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
      if (maxScale == null) {
        maxScale = 20;
      }
      Integer maxSize = exponentialBucketHistogram.getMaxSize();
      if (maxSize == null) {
        maxSize = 160;
      }
      Boolean recordMinMax = exponentialBucketHistogram.getRecordMinMax();
      HistogramOptions options =
          HistogramOptions.builder()
              .setRecordMinMax(recordMinMax != null ? recordMinMax : true)
              .build();
      try {
        return Aggregation.base2ExponentialBucketHistogram(maxSize, maxScale, options);
      } catch (IllegalArgumentException e) {
        throw new DeclarativeConfigException("Invalid exponential bucket histogram", e);
      }
    }
    ExplicitBucketHistogramAggregationModel explicitBucketHistogram =
        model.getExplicitBucketHistogram();
    if (explicitBucketHistogram != null) {
      List<Double> boundaries = explicitBucketHistogram.getBoundaries();
      Boolean recordMinMax = explicitBucketHistogram.getRecordMinMax();
      HistogramOptions options =
          HistogramOptions.builder()
              .setRecordMinMax(recordMinMax != null ? recordMinMax : true)
              .build();
      if (boundaries == null) {
        return Aggregation.explicitBucketHistogram(
            ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES, options);
      }
      try {
        return Aggregation.explicitBucketHistogram(boundaries, options);
      } catch (IllegalArgumentException e) {
        throw new DeclarativeConfigException("Invalid explicit bucket histogram", e);
      }
    }

    return Aggregation.defaultAggregation();
  }
}
