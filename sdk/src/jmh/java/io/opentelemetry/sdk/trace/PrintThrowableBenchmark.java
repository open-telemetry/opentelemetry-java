package io.opentelemetry.sdk.trace;

import com.google.common.io.CharStreams;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public class PrintThrowableBenchmark {

  private static final int THROWABLE_SIZE = 50;

  private static final Throwable THROWABLE;

  static {
    Throwable throwable = null;
    try {
      throwAfter(THROWABLE_SIZE);
    } catch (Throwable t) {
      throwable = t;
    }
    if (throwable == null) {
      throw new AssertionError();
    }
    THROWABLE = throwable;
  }

  private static void throwAfter(int count) {
    if (count == THROWABLE_SIZE) {
      throw new AssertionError("threw");
    } else {
      throwAfter(count + 1);
    }
  }

  @Benchmark
  public String normalPrintWriter() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new PrintWriter(CharStreams.asWriter(sb));
    THROWABLE.printStackTrace(writer);
    return sb.toString();
  }

  @Benchmark
  public String stringWriter() {
    StringWriter sw = new StringWriter();
    PrintWriter writer = new PrintWriter(sw);
    THROWABLE.printStackTrace(writer);
    return sw.toString();
  }

  @Benchmark
  public String customPrintWriter() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new StringBuilderPrintWriter(sb);
    THROWABLE.printStackTrace(writer);
    return sb.toString();
  }
}
