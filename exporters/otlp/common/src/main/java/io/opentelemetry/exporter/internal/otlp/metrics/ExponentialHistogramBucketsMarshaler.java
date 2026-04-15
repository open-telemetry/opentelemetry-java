/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogramDataPoint;
import io.opentelemetry.sdk.common.internal.DynamicPrimitiveLongList;
import io.opentelemetry.sdk.common.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.io.IOException;
import java.util.List;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
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
    if (counts instanceof DynamicPrimitiveLongList) {
      output.serializeRepeatedUInt64(
          ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS, (DynamicPrimitiveLongList) counts);
    } else {
      output.serializeRepeatedUInt64(
          ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS, PrimitiveLongList.toArray(counts));
    }
  }

  static int calculateSize(int offset, List<Long> counts) {
    int size = 0;
    size += MarshalerUtil.sizeSInt32(ExponentialHistogramDataPoint.Buckets.OFFSET, offset);
    if (counts instanceof DynamicPrimitiveLongList) {
      size +=
          MarshalerUtil.sizeRepeatedUInt64(
              ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS,
              (DynamicPrimitiveLongList) counts);
    } else {
      size +=
          MarshalerUtil.sizeRepeatedUInt64(
              ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS,
              PrimitiveLongList.toArray(counts));
    }
    return size;
  }
}
