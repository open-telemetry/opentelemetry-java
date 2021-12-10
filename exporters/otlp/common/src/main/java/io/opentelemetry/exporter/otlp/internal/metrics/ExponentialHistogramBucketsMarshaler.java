/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogramDataPoint;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.io.IOException;
import java.util.List;

public class ExponentialHistogramBucketsMarshaler extends MarshalerWithSize {
  private final int offset;
  private final List<Long> counts;

  static ExponentialHistogramBucketsMarshaler create(ExponentialHistogramBuckets buckets) {
    return new ExponentialHistogramBucketsMarshaler(buckets.getOffset(), buckets.getBucketCounts());
  }

  private ExponentialHistogramBucketsMarshaler(int offset, List<Long> counts) {
    super(calculateSize(offset, counts));
    this.offset = offset;
    this.counts = counts;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeSInt32(ExponentialHistogramDataPoint.Buckets.OFFSET, offset);
    output.serializeRepeatedUInt64(ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS, counts);
  }

  private static int calculateSize(int offset, List<Long> counts) {
    int size = 0;
    size += MarshalerUtil.sizeSInt32(ExponentialHistogramDataPoint.Buckets.OFFSET, offset);
    size +=
        MarshalerUtil.sizeRepeatedUInt64(
            ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS, counts);
    return size;
  }
}
