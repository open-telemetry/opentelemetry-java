/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import com.google.common.io.CharStreams;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
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

  /** Measures performance of {@link StringBuilder} + Guava {@link CharStreams}. */
  @Benchmark
  public String normalPrintWriter() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new PrintWriter(CharStreams.asWriter(sb));
    THROWABLE.printStackTrace(writer);
    return sb.toString();
  }

  /** Measures performance of JDK {@link StringWriter}. */
  @Benchmark
  public String stringWriter() {
    StringWriter sw = new StringWriter();
    PrintWriter writer = new PrintWriter(sw);
    THROWABLE.printStackTrace(writer);
    return sw.toString();
  }

  /** Measures performance of a {@link PrintStream}. */
  @Benchmark
  public String printStream() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(bos);
    THROWABLE.printStackTrace(stream);
    return bos.toString(StandardCharsets.UTF_8.name());
  }
}
