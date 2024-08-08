/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class MetricAdviceBenchmark {

  private static final Attributes ALL_ATTRIBUTES;
  private static final Attributes SOME_ATTRIBUTES;
  private static final List<AttributeKey<?>> SOME_ATTRIBUTE_KEYS;

  static {
    SOME_ATTRIBUTES =
        Attributes.builder()
            .put("http.request.method", "GET")
            .put("http.route", "/v1/users/{id}")
            .put("http.response.status_code", 200)
            .build();
    ALL_ATTRIBUTES =
        SOME_ATTRIBUTES.toBuilder().put("http.url", "http://localhost:8080/v1/users/123").build();
    SOME_ATTRIBUTE_KEYS = new ArrayList<>(SOME_ATTRIBUTES.asMap().keySet());
  }

  @State(Scope.Benchmark)
  public static class ThreadState {

    @Param InstrumentParam instrumentParam;

    SdkMeterProvider meterProvider;

    @Setup(Level.Iteration)
    public void setup() {
      meterProvider =
          SdkMeterProvider.builder()
              .registerMetricReader(InMemoryMetricReader.createDelta())
              .build();
      Meter meter = meterProvider.get("meter");
      instrumentParam.instrument().setup(meter);
    }

    @TearDown
    public void tearDown() {
      meterProvider.shutdown().join(10, TimeUnit.SECONDS);
    }
  }

  @Benchmark
  @Threads(1)
  public void record(ThreadState threadState) {
    threadState.instrumentParam.instrument().record(1);
  }

  @SuppressWarnings("ImmutableEnumChecker")
  public enum InstrumentParam {
    NO_ADVICE_RECORD_ALL(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter = ((ExtendedLongCounterBuilder) meter.counterBuilder("counter")).build();
          }

          @Override
          void record(long value) {
            counter.add(value, ALL_ATTRIBUTES);
          }
        }),
    ADVICE_RECORD_ALL(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter =
                ((ExtendedLongCounterBuilder) meter.counterBuilder("counter"))
                    .setAttributesAdvice(SOME_ATTRIBUTE_KEYS)
                    .build();
          }

          @Override
          void record(long value) {
            counter.add(value, ALL_ATTRIBUTES);
          }
        }),
    ADVICE_RECORD_SOME(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter =
                ((ExtendedLongCounterBuilder) meter.counterBuilder("counter"))
                    .setAttributesAdvice(SOME_ATTRIBUTE_KEYS)
                    .build();
          }

          @Override
          void record(long value) {
            counter.add(value, SOME_ATTRIBUTES);
          }
        });

    private final Instrument instrument;

    InstrumentParam(Instrument instrument) {
      this.instrument = instrument;
    }

    Instrument instrument() {
      return instrument;
    }
  }

  private abstract static class Instrument {
    abstract void setup(Meter meter);

    abstract void record(long value);
  }
}
