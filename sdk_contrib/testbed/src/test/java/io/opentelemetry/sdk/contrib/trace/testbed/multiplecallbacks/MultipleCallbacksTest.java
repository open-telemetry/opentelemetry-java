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

package io.opentelemetry.sdk.contrib.trace.testbed.multiplecallbacks;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.contrib.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * These tests are intended to simulate a task with independent, asynchronous callbacks.
 *
 * <p>For improved readability, ignore the CountDownLatch lines as those are there to ensure
 * deterministic execution for the tests without sleeps.
 */
@SuppressWarnings("FutureReturnValueIgnored")
public class MultipleCallbacksTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = TestUtils.createTracerShim(exporter);

  @Test
  public void test() {
    CountDownLatch parentDoneLatch = new CountDownLatch(1);
    Client client = new Client(tracer, parentDoneLatch);

    Span span = tracer.spanBuilder("parent").startSpan();
    try (Scope scope = tracer.withSpan(span)) {
      client.send("task1");
      client.send("task2");
      client.send("task3");
    } finally {
      span.end();
      parentDoneLatch.countDown();
    }

    await().atMost(15, TimeUnit.SECONDS).until(TestUtils.finishedSpansSize(exporter), equalTo(4));

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(4);
    assertThat(spans.get(0).getName()).isEqualTo("parent");

    SpanData parentSpan = spans.get(0);
    for (int i = 1; i < 4; i++) {
      assertThat(spans.get(i).getContext().getTraceId()).isEqualTo(parentSpan.getTraceId());
      assertThat(spans.get(i).getParentSpanId()).isEqualTo(parentSpan.getSpanId());
    }

    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(DefaultSpan.getInvalid());
  }
}
