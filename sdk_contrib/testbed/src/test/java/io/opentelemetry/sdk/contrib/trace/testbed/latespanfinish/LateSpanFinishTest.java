/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.contrib.trace.testbed.latespanfinish;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.contrib.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

@SuppressWarnings("FutureReturnValueIgnored")
public final class LateSpanFinishTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = TestUtils.createTracerShim(exporter);
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  public void test() throws Exception {
    // Create a Span manually and use it as parent of a pair of subtasks
    Span parentSpan = tracer.spanBuilder("parent").startSpan();
    submitTasks(parentSpan);

    // Wait for the threadpool to be done first, instead of polling/waiting
    executor.shutdown();
    executor.awaitTermination(15, TimeUnit.SECONDS);

    // Late-finish the parent Span now
    parentSpan.end();

    // Children finish order is not guaranteed, but parent should finish *last*.
    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(3);
    assertThat(spans.get(0).getName()).startsWith("task");
    assertThat(spans.get(1).getName()).startsWith("task");
    assertThat(spans.get(2).getName()).isEqualTo("parent");

    TestUtils.assertSameTrace(spans);

    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  /*
   * Fire away a few subtasks, passing a parent Span whose lifetime
   * is not tied at-all to the children
   */
  private void submitTasks(final Span parentSpan) {

    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            /* Alternative to calling activate() is to pass it manually to asChildOf() for each
             * created Span. */
            try (Scope scope = tracer.withSpan(parentSpan)) {
              Span childSpan = tracer.spanBuilder("task1").startSpan();
              try (Scope childScope = tracer.withSpan(childSpan)) {
                TestUtils.sleep(55);
              } finally {
                childSpan.end();
              }
            }
          }
        });

    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            try (Scope scope = tracer.withSpan(parentSpan)) {
              Span childSpan = tracer.spanBuilder("task2").startSpan();
              try (Scope childScope = tracer.withSpan(childSpan)) {
                TestUtils.sleep(85);
              } finally {
                childSpan.end();
              }
            }
          }
        });
  }
}
