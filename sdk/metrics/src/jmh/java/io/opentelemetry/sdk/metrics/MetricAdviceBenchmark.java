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
import java.util.Arrays;
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

  static final AttributeKey<String> HTTP_REQUEST_METHOD =
      AttributeKey.stringKey("http.request.method");
  static final AttributeKey<String> URL_PATH = AttributeKey.stringKey("url.path");
  static final AttributeKey<String> URL_SCHEME = AttributeKey.stringKey("url.scheme");
  static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE =
      AttributeKey.longKey("http.response.status_code");
  static final AttributeKey<String> HTTP_ROUTE = AttributeKey.stringKey("http.route");
  static final AttributeKey<String> NETWORK_PROTOCOL_NAME =
      AttributeKey.stringKey("network.protocol.name");
  static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");
  static final AttributeKey<String> URL_QUERY = AttributeKey.stringKey("url.query");
  static final AttributeKey<String> CLIENT_ADDRESS = AttributeKey.stringKey("client.address");
  static final AttributeKey<String> NETWORK_PEER_ADDRESS =
      AttributeKey.stringKey("network.peer.address");
  static final AttributeKey<Long> NETWORK_PEER_PORT = AttributeKey.longKey("network.peer.port");
  static final AttributeKey<String> NETWORK_PROTOCOL_VERSION =
      AttributeKey.stringKey("network.protocol.version");
  static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
  static final AttributeKey<String> USER_AGENT_ORIGINAL =
      AttributeKey.stringKey("user_agent.original");

  static final List<AttributeKey<?>> httpServerMetricAttributeKeys =
      Arrays.asList(
          HTTP_REQUEST_METHOD,
          URL_SCHEME,
          HTTP_RESPONSE_STATUS_CODE,
          HTTP_ROUTE,
          NETWORK_PROTOCOL_NAME,
          SERVER_PORT,
          NETWORK_PROTOCOL_VERSION,
          SERVER_ADDRESS);

  static Attributes httpServerMetricAttributes() {
    return Attributes.builder()
        .put(HTTP_REQUEST_METHOD, "GET")
        .put(URL_SCHEME, "http")
        .put(HTTP_RESPONSE_STATUS_CODE, 200)
        .put(HTTP_ROUTE, "/v1/users/{id}")
        .put(NETWORK_PROTOCOL_NAME, "http")
        .put(SERVER_PORT, 8080)
        .put(NETWORK_PROTOCOL_VERSION, "1.1")
        .put(SERVER_ADDRESS, "localhost")
        .build();
  }

  static Attributes httpServerSpanAttributes() {
    return Attributes.builder()
        .put(HTTP_REQUEST_METHOD, "GET")
        .put(URL_PATH, "/v1/users/123")
        .put(URL_SCHEME, "http")
        .put(HTTP_RESPONSE_STATUS_CODE, 200)
        .put(HTTP_ROUTE, "/v1/users/{id}")
        .put(NETWORK_PROTOCOL_NAME, "http")
        .put(SERVER_PORT, 8080)
        .put(URL_QUERY, "with=email")
        .put(CLIENT_ADDRESS, "192.168.0.17")
        .put(NETWORK_PEER_ADDRESS, "192.168.0.17")
        .put(NETWORK_PEER_PORT, 11265)
        .put(NETWORK_PROTOCOL_VERSION, "1.1")
        .put(SERVER_ADDRESS, "localhost")
        .put(USER_AGENT_ORIGINAL, "okhttp/1.27.2")
        .build();
  }

  static final Attributes CACHED_HTTP_SERVER_SPAN_ATTRIBUTES = httpServerSpanAttributes();

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
    /**
     * Record HTTP span attributes without advice. This baseline shows the CPU and memory allocation
     * independent of advice.
     */
    NO_ADVICE_ALL_ATTRIBUTES(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter = ((ExtendedLongCounterBuilder) meter.counterBuilder("counter")).build();
          }

          @Override
          void record(long value) {
            counter.add(value, httpServerSpanAttributes());
          }
        }),
    /**
     * Record HTTP metric attributes without advice. This baseline shows the lower bound if
     * attribute filtering was done in instrumentation instead of the metrics SDK with advice. It's
     * not quite fair though because instrumentation would have to separately allocate attributes
     * for spans and metrics, whereas with advice, we can manage to only allocate span attributes
     * and a lightweight metrics attributes view derived from span attributes.
     */
    NO_ADVICE_FILTERED_ATTRIBUTES(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter = ((ExtendedLongCounterBuilder) meter.counterBuilder("counter")).build();
          }

          @Override
          void record(long value) {
            counter.add(value, httpServerMetricAttributes());
          }
        }),
    /**
     * Record cached HTTP span attributes without advice. This baseline helps isolate the CPU and
     * memory allocation for recording vs. creating attributes.
     */
    NO_ADVICE_ALL_ATTRIBUTES_CACHED(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter = ((ExtendedLongCounterBuilder) meter.counterBuilder("counter")).build();
          }

          @Override
          void record(long value) {
            counter.add(value, CACHED_HTTP_SERVER_SPAN_ATTRIBUTES);
          }
        }),
    /**
     * Record HTTP span attributes with advice filtering to HTTP metric attributes. This is meant to
     * realistically demonstrate a typical HTTP server instrumentation scenario.
     */
    ADVICE_ALL_ATTRIBUTES(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter =
                ((ExtendedLongCounterBuilder) meter.counterBuilder("counter"))
                    .setAttributesAdvice(httpServerMetricAttributeKeys)
                    .build();
          }

          @Override
          void record(long value) {
            counter.add(value, httpServerSpanAttributes());
          }
        }),
    /**
     * Record HTTP metric attributes with advice filtering to HTTP metric attributes. This
     * demonstrates the overhead of advice when no attributes are filtered.
     */
    ADVICE_FILTERED_ATTRIBUTES(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter =
                ((ExtendedLongCounterBuilder) meter.counterBuilder("counter"))
                    .setAttributesAdvice(httpServerMetricAttributeKeys)
                    .build();
          }

          @Override
          void record(long value) {
            counter.add(value, httpServerMetricAttributes());
          }
        }),
    /**
     * Record cached HTTP span attributes with advice filtering to HTTP metric attributes. This
     * isolates the CPU and memory allocation for applying advice vs. creating attributes.
     */
    ADVICE_ALL_ATTRIBUTES_CACHED(
        new Instrument() {
          private LongCounter counter;

          @Override
          void setup(Meter meter) {
            counter =
                ((ExtendedLongCounterBuilder) meter.counterBuilder("counter"))
                    .setAttributesAdvice(httpServerMetricAttributeKeys)
                    .build();
          }

          @Override
          void record(long value) {
            counter.add(value, CACHED_HTTP_SERVER_SPAN_ATTRIBUTES);
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
