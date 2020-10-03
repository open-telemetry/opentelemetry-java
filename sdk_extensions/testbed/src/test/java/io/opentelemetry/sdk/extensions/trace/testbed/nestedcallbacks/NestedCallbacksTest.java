/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.nestedcallbacks;

import static io.opentelemetry.common.AttributeKey.stringKey;
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
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
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
      assertThat(attrs.get(stringKey("key" + i))).isEqualTo(Integer.toString(i));
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
