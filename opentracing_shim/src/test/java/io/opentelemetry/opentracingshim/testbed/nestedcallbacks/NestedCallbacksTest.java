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

package io.opentelemetry.opentracingshim.testbed.nestedcallbacks;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.createTracerShim;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.AttributeValue;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

@SuppressWarnings("FutureReturnValueIgnored")
public final class NestedCallbacksTest {

  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = createTracerShim(exporter);
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  public void test() throws Exception {

    Span span = tracer.buildSpan("one").start();
    submitCallbacks(span);

    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(exporter), equalTo(1));

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertEquals(1, spans.size());
    assertEquals("one", spans.get(0).getName());

    Map<String, AttributeValue> attrs = spans.get(0).getAttributes();
    assertEquals(3, attrs.size());
    for (int i = 1; i <= 3; i++) {
      assertEquals(Integer.toString(i), attrs.get("key" + i).getStringValue());
    }

    assertNull(tracer.scopeManager().activeSpan());
  }

  private void submitCallbacks(final Span span) {

    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            try (Scope scope = tracer.scopeManager().activate(span)) {
              span.setTag("key1", "1");

              executor.submit(
                  new Runnable() {
                    @Override
                    public void run() {
                      try (Scope scope = tracer.scopeManager().activate(span)) {
                        span.setTag("key2", "2");

                        executor.submit(
                            new Runnable() {
                              @Override
                              public void run() {
                                try (Scope scope = tracer.scopeManager().activate(span)) {
                                  span.setTag("key3", "3");
                                } finally {
                                  span.finish();
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
