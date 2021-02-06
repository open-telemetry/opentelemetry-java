/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.promisepropagation;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.testbed.nestedcallbacks.NestedCallbacksTest;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 *
 * <p>For improved readability, ignore the phaser lines as those are there to ensure deterministic
 * execution for the tests without sleeps.
 */
class PromisePropagationTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(NestedCallbacksTest.class.getName());
  private Phaser phaser;

  @BeforeEach
  void before() {
    phaser = new Phaser();
  }

  @Test
  void testPromiseCallback() {
    phaser.register(); // register test thread
    final AtomicReference<String> successResult1 = new AtomicReference<>();
    final AtomicReference<String> successResult2 = new AtomicReference<>();
    final AtomicReference<Throwable> errorResult = new AtomicReference<>();

    try (PromiseContext context = new PromiseContext(phaser, 3)) {
      Span parentSpan = tracer.spanBuilder("promises").startSpan();
      parentSpan.setAttribute("component", "example-promises");

      try (Scope ignored = parentSpan.makeCurrent()) {
        Promise<String> successPromise = new Promise<>(context, tracer);

        successPromise.onSuccess(
            s -> {
              Span.current().addEvent("Promised 1 " + s);
              successResult1.set(s);
              phaser.arriveAndAwaitAdvance(); // result set
            });
        successPromise.onSuccess(
            s -> {
              Span.current().addEvent("Promised 2 " + s);
              successResult2.set(s);
              phaser.arriveAndAwaitAdvance(); // result set
            });

        Promise<String> errorPromise = new Promise<>(context, tracer);

        errorPromise.onError(
            t -> {
              errorResult.set(t);
              phaser.arriveAndAwaitAdvance(); // result set
            });

        assertThat(otelTesting.getSpans().size()).isEqualTo(0);
        successPromise.success("success!");
        errorPromise.error(new Exception("some error."));
      } finally {
        parentSpan.end();
      }

      phaser.arriveAndAwaitAdvance(); // wait for results to be set
      assertThat(successResult1.get()).isEqualTo("success!");
      assertThat(successResult2.get()).isEqualTo("success!");
      assertThat(errorResult.get().getMessage()).isEqualTo("some error.");

      phaser.arriveAndAwaitAdvance(); // wait for traces to be reported

      List<SpanData> finished = otelTesting.getSpans();
      assertThat(finished.size()).isEqualTo(4);

      AttributeKey<String> component = stringKey("component");
      SpanData parentSpanProto = TestUtils.getOneByAttr(finished, component, "example-promises");
      assertThat(parentSpanProto).isNotNull();
      assertThat(SpanId.isValid(parentSpanProto.getParentSpanIdHex())).isFalse();
      List<SpanData> successSpans = TestUtils.getByAttr(finished, component, "success");
      assertThat(successSpans).hasSize(2);

      CharSequence parentId = parentSpanProto.getSpanIdHex();
      for (SpanData span : successSpans) {
        assertThat(span.getParentSpanIdHex()).isEqualTo(parentId.toString());
      }

      SpanData errorSpan = TestUtils.getOneByAttr(finished, component, "error");
      assertThat(errorSpan).isNotNull();
      assertThat(errorSpan.getParentSpanIdHex()).isEqualTo(parentId.toString());
    }
  }
}
