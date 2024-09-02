/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of {@link ExtendedSpanProcessor}. */
class ExtendedSpanProcessorUsageTest {

  private static final AttributeKey<String> FOO_KEY = AttributeKey.stringKey("foo");
  private static final AttributeKey<String> BAR_KEY = AttributeKey.stringKey("bar");

  private static class CopyFooToBarProcessor implements ExtendedSpanProcessor {

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {}

    @Override
    public boolean isStartRequired() {
      return false;
    }

    @Override
    public void onEnd(ReadableSpan span) {}

    @Override
    public boolean isEndRequired() {
      return false;
    }

    @Override
    public void onEnding(ReadWriteSpan span) {
      String val = span.getAttribute(FOO_KEY);
      span.setAttribute(BAR_KEY, val);
    }

    @Override
    public boolean isOnEndingRequired() {
      return true;
    }
  }

  @Test
  public void extendedSpanProcessorUsage() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();

    try (SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .addSpanProcessor(new CopyFooToBarProcessor())
            .build()) {

      Tracer tracer = tracerProvider.get("dummy-tracer");
      Span span = tracer.spanBuilder("my-span").startSpan();

      span.setAttribute(FOO_KEY, "Hello!");

      span.end();

      assertThat(exporter.getFinishedSpanItems())
          .hasSize(1)
          .first()
          .satisfies(
              spanData -> {
                assertThat(spanData.getAttributes())
                    .containsEntry(FOO_KEY, "Hello!")
                    .containsEntry(BAR_KEY, "Hello!");
              });
    }
  }
}
