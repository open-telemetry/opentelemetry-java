package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class MultiSpanExporterBenchmark {

  private static class NoopSpanExporter implements SpanExporter {

    @Override
    public ResultCode export(List<SpanData> spans) {
      return ResultCode.SUCCESS;
    }

    @Override
    public void shutdown() {}
  }

  @Param({"1", "3"})
  private int exporterCount;

  private SpanExporter exporter;

  @Param({"1000"})
  private int spanCount;

  private List<SpanData> spans;

  @Setup(Level.Trial)
  public final void setup() {
    SpanExporter[] exporter = new SpanExporter[exporterCount];
    Arrays.fill(exporter, new NoopSpanExporter());
    this.exporter = MultiSpanExporter.create(Arrays.asList(exporter));

    SpanData[] spans = new SpanData[spanCount];
    for (int i = 0; i < spans.length; i++) {
      spans[i] =
          SpanData.newBuilder()
              .setTraceId(new TraceId(1, 1))
              .setSpanId(new SpanId(1))
              .setName("noop")
              .setKind(Span.Kind.CLIENT)
              .setStartEpochNanos(1)
              .setStatus(Status.OK)
              .setEndEpochNanos(2)
              .build();
    }
    this.spans = Arrays.asList(spans);
  }

  @Benchmark
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public ResultCode export() {
    return exporter.export(spans);
  }
}
