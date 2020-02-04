package io.opentelemetry.trace.propagation;

import io.opentelemetry.context.propagation.HttpTextFormat.Getter;
import io.opentelemetry.trace.SpanContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
public class HtpTraceContextExtractBenchmark {

  private String traceIdBase16 = "ff000000000000000000000000000041";
  private String spanIdBase16 = "ff00000000000041";
  private String traceparent = "traceparent";
  private String traceparentHeaderSampled = "00-" + traceIdBase16 + "-" + spanIdBase16 + "-01";
  private HttpTraceContext httpTraceContext;
  private Map<String, String> carrier;
  private Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  @Setup
  public void setup() {
    this.httpTraceContext = new HttpTraceContext();
    this.carrier = new LinkedHashMap<>();
    this.carrier.put(traceparent, traceparentHeaderSampled);
  }

  @Benchmark
  @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
  public SpanContext measureExtract() {
    return httpTraceContext.extract(carrier, getter);
  }

  @TearDown(Level.Iteration)
  public void refreshCarrier() {
    this.carrier = new LinkedHashMap<>();
    this.carrier.put(traceparent, traceparentHeaderSampled);
  }
}
