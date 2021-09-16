/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import io.opentelemetry.exporter.jaeger.internal.protobuf.internal.Time;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

// The wire format for Timestamp and Duration are exactly the same. Just implement one Marshaler
// for them.
final class TimeMarshaler extends MarshalerWithSize {
  private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);

  static TimeMarshaler create(long timeNanos) {
    long seconds = timeNanos / NANOS_PER_SECOND;
    int nanos = (int) (timeNanos % NANOS_PER_SECOND);
    return new TimeMarshaler(seconds, nanos);
  }

  private final long seconds;
  private final int nanos;

  TimeMarshaler(long seconds, int nanos) {
    super(calculateSize(seconds, nanos));
    this.seconds = seconds;
    this.nanos = nanos;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt64(Time.SECONDS, seconds);
    output.serializeInt32(Time.NANOS, nanos);
  }

  private static int calculateSize(long seconds, int nanos) {
    int size = 0;
    size += MarshalerUtil.sizeInt64(Time.SECONDS, seconds);
    size += MarshalerUtil.sizeInt32(Time.NANOS, nanos);
    return size;
  }
}
