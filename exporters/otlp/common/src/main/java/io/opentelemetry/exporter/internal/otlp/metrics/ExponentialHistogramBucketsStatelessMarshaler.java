/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogramDataPoint;
import io.opentelemetry.sdk.common.internal.DynamicPrimitiveLongList;
import io.opentelemetry.sdk.common.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.io.IOException;
import java.util.List;

/** See {@link ExponentialHistogramBucketsMarshaler}. */
final class ExponentialHistogramBucketsStatelessMarshaler
    implements StatelessMarshaler<ExponentialHistogramBuckets> {
  static final ExponentialHistogramBucketsStatelessMarshaler INSTANCE =
      new ExponentialHistogramBucketsStatelessMarshaler();

  private ExponentialHistogramBucketsStatelessMarshaler() {}

  @Override
  public void writeTo(
      Serializer output, ExponentialHistogramBuckets buckets, MarshalerContext context)
      throws IOException {
    output.serializeSInt32(ExponentialHistogramDataPoint.Buckets.OFFSET, buckets.getOffset());
    List<Long> counts = buckets.getBucketCounts();
    if (counts instanceof DynamicPrimitiveLongList) {
      output.serializeRepeatedUInt64(
          ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS, (DynamicPrimitiveLongList) counts);
    } else {
      output.serializeRepeatedUInt64(
          ExponentialHistogramDataPoint.Buckets.BUCKET_COUNTS, PrimitiveLongList.toArray(counts));
    }
  }

  @Override
  public int getBinarySerializedSize(
      ExponentialHistogramBuckets buckets, MarshalerContext context) {
    return ExponentialHistogramBucketsMarshaler.calculateSize(
        buckets.getOffset(), buckets.getBucketCounts());
  }
}
