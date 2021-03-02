/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Threads(value = 1)
@Fork(3)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ContextBenchmark {

  @Param({"2", "3", "4", "5", "10", "20", "40"})
  private int size;

  private int middle;

  private List<ContextKey<String>> keys;
  private Context context = Context.root();

  @Setup
  public void setup() {
    keys = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ContextKey<String> key = ContextKey.named(Integer.toString(i));
      context = context.with(key, "value");
      keys.add(key);
    }
    middle = size / 2;
  }

  @Benchmark
  public String readFirst() {
    return context.get(keys.get(0));
  }

  @Benchmark
  public String readLast() {
    return context.get(keys.get(size - 1));
  }

  @Benchmark
  public String readMiddle() {
    return context.get(keys.get(middle));
  }

  @Benchmark
  public void readAll(Blackhole bh) {
    for (int i = 0; i < size; i++) {
      bh.consume(context.get(keys.get(i)));
    }
  }

  @Benchmark
  public Context writeOne() {
    return Context.root().with(keys.get(0), "value");
  }

  @Benchmark
  public Context writeAll() {
    Context context = Context.root();
    for (int i = 0; i < size; i++) {
      context = context.with(keys.get(i), "value");
    }
    return context;
  }
}
