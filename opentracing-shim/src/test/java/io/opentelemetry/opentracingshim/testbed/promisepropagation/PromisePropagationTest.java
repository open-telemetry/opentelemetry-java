/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.promisepropagation;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.opentracingshim.testbed.TestUtils.getByAttr;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanIdHex;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
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
 *
 * @author tylerbenson
 */
class PromisePropagationTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
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
      Span parentSpan =
          tracer.buildSpan("promises").withTag(Tags.COMPONENT.getKey(), "example-promises").start();

      try (Scope parentScope = tracer.activateSpan(parentSpan)) {
        Promise<String> successPromise = new Promise<>(context, tracer);

        successPromise.onSuccess(
            s -> {
              tracer.activeSpan().log("Promised 1 " + s);
              successResult1.set(s);
              phaser.arriveAndAwaitAdvance(); // result set
            });
        successPromise.onSuccess(
            s -> {
              tracer.activeSpan().log("Promised 2 " + s);
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
        parentSpan.finish();
      }

      phaser.arriveAndAwaitAdvance(); // wait for results to be set
      assertThat(successResult1.get()).isEqualTo("success!");
      assertThat(successResult2.get()).isEqualTo("success!");
      assertThat(errorResult.get()).hasMessage("some error.");

      phaser.arriveAndAwaitAdvance(); // wait for traces to be reported

      List<SpanData> finished = otelTesting.getSpans();
      assertThat(finished.size()).isEqualTo(4);

      AttributeKey<String> component = stringKey(Tags.COMPONENT.getKey());
      List<SpanData> spanExamplePromise = getByAttr(finished, component, "example-promises");
      assertThat(spanExamplePromise).hasSize(1);
      assertThat(spanExamplePromise.get(0).getParentSpanId()).isEqualTo(SpanIdHex.getInvalid());

      assertThat(getByAttr(finished, component, "success")).hasSize(2);

      CharSequence parentId = spanExamplePromise.get(0).getSpanId();
      for (SpanData span : getByAttr(finished, component, "success")) {
        assertThat(span.getParentSpanId()).isEqualTo(parentId.toString());
      }

      List<SpanData> spanError = getByAttr(finished, component, "error");
      assertThat(spanError).hasSize(1);
      assertThat(spanError.get(0).getParentSpanId()).isEqualTo(parentId.toString());
    }
  }
}
