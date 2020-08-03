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

package io.opentelemetry.sdk.extensions.trace.testbed.nestedcallbacks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.extensions.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@SuppressWarnings("FutureReturnValueIgnored")
public final class NestedCallbacksTest {

  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerProvider(sdk).build();
  private final Tracer tracer = sdk.get(NestedCallbacksTest.class.getName());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test() {

    Span span = tracer.spanBuilder("one").startSpan();
    submitCallbacks(span);

    await()
        .atMost(15, TimeUnit.SECONDS)
        .until(TestUtils.finishedSpansSize(inMemoryTracing.getSpanExporter()), equalTo(1));

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getName()).isEqualTo("one");

    ReadableAttributes attrs = spans.get(0).getAttributes();
    assertThat(attrs.size()).isEqualTo(3);
    for (int i = 1; i <= 3; i++) {
      assertThat(attrs.get("key" + i).getStringValue()).isEqualTo(Integer.toString(i));
    }

    assertThat(tracer.getCurrentSpan()).isSameAs(DefaultSpan.getInvalid());
  }

  private void submitCallbacks(final Span span) {

    executor.submit(
        () -> {
          try (Scope ignored = tracer.withSpan(span)) {
            span.setAttribute("key1", "1");

            executor.submit(
                () -> {
                  try (Scope ignored12 = tracer.withSpan(span)) {
                    span.setAttribute("key2", "2");

                    executor.submit(
                        () -> {
                          try (Scope ignored1 = tracer.withSpan(span)) {
                            span.setAttribute("key3", "3");
                          } finally {
                            span.end();
                          }
                        });
                  }
                });
          }
        });
  }
}
