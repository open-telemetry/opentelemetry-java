/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.nestedcallbacks;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("FutureReturnValueIgnored")
public final class NestedCallbacksTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test() {

    Span span = tracer.buildSpan("one").start();
    submitCallbacks(span);

    await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(otelTesting), equalTo(1));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getName()).isEqualTo("one");

    ReadableAttributes attrs = spans.get(0).getAttributes();
    assertThat(attrs.size()).isEqualTo(3);
    for (int i = 1; i <= 3; i++) {
      assertThat(spans.get(0).getAttributes().get(stringKey("key" + i)))
          .isEqualTo(Integer.toString(i));
    }

    assertThat(tracer.scopeManager().activeSpan()).isNull();
  }

  private void submitCallbacks(final Span span) {

    executor.submit(
        () -> {
          try (Scope scope = tracer.scopeManager().activate(span)) {
            span.setTag("key1", "1");

            executor.submit(
                () -> {
                  try (Scope scope12 = tracer.scopeManager().activate(span)) {
                    span.setTag("key2", "2");

                    executor.submit(
                        () -> {
                          try (Scope scope1 = tracer.scopeManager().activate(span)) {
                            span.setTag("key3", "3");
                          } finally {
                            span.finish();
                          }
                        });
                  }
                });
          }
        });
  }
}
