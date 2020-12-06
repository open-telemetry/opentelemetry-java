/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.nestedcallbacks;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("FutureReturnValueIgnored")
public final class NestedCallbacksTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(NestedCallbacksTest.class.getName());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  void test() {

    Span span = tracer.spanBuilder("one").startSpan();
    submitCallbacks(span);

    await()
        .atMost(Duration.ofSeconds(15))
        .until(TestUtils.finishedSpansSize(otelTesting), equalTo(1));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getName()).isEqualTo("one");

    Attributes attrs = spans.get(0).getAttributes();
    assertThat(attrs.size()).isEqualTo(3);
    for (int i = 1; i <= 3; i++) {
      assertThat(attrs.get(stringKey("key" + i))).isEqualTo(Integer.toString(i));
    }

    assertThat(Span.current()).isSameAs(Span.getInvalid());
  }

  private void submitCallbacks(final Span span) {

    executor.submit(
        () -> {
          try (Scope ignored = span.makeCurrent()) {
            span.setAttribute("key1", "1");

            executor.submit(
                () -> {
                  try (Scope ignored12 = span.makeCurrent()) {
                    span.setAttribute("key2", "2");

                    executor.submit(
                        () -> {
                          try (Scope ignored1 = span.makeCurrent()) {
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
