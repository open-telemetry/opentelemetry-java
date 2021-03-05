/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanBuilder;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Threads(value = 1)
@Fork(3)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 20, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class FillSpanBenchmark {

  private static final SpanBuilder spanBuilder =
      SdkTracerProvider.builder().build().get("benchmark").spanBuilder("benchmark");

  private static final AttributeKey<String> KEY1 = AttributeKey.stringKey("key1");
  private static final AttributeKey<String> KEY2 = AttributeKey.stringKey("key2");
  private static final AttributeKey<String> KEY3 = AttributeKey.stringKey("key3");
  private static final AttributeKey<String> KEY4 = AttributeKey.stringKey("key4");

  @Benchmark
  public void setFourAttributes() {
    spanBuilder
        .startSpan()
        .setAttribute(KEY1, "value1")
        .setAttribute(KEY2, "value2")
        .setAttribute(KEY3, "value3")
        .setAttribute(KEY4, "value4");
  }
}
