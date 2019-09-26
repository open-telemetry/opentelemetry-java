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

package io.opentelemetry.sdk.contrib.trace.testbed.nestedcallbacks;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.contrib.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

@SuppressWarnings("FutureReturnValueIgnored")
public final class NestedCallbacksTest {

  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = TestUtils.createTracerShim(exporter);
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  public void test() {

    Span span = tracer.spanBuilder("one").startSpan();
    submitCallbacks(span);

    await().atMost(15, TimeUnit.SECONDS).until(TestUtils.finishedSpansSize(exporter), equalTo(1));

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getName()).isEqualTo("one");

    Map<String, AttributeValue> attrs = spans.get(0).getAttributes();
    assertThat(attrs).hasSize(3);
    for (int i = 1; i <= 3; i++) {
      assertThat(attrs.get("key" + i).getStringValue()).isEqualTo(Integer.toString(i));
    }

    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  private void submitCallbacks(final Span span) {

    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            try (Scope ignored = tracer.withSpan(span)) {
              span.setAttribute("key1", "1");

              executor.submit(
                  new Runnable() {
                    @Override
                    public void run() {
                      try (Scope ignored = tracer.withSpan(span)) {
                        span.setAttribute("key2", "2");

                        executor.submit(
                            new Runnable() {
                              @Override
                              public void run() {
                                try (Scope ignored = tracer.withSpan(span)) {
                                  span.setAttribute("key3", "3");
                                } finally {
                                  span.end();
                                }
                              }
                            });
                      }
                    }
                  });
            }
          }
        });
  }
}
