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

package io.opentelemetry.opentracingshim.testbed.promisepropagation;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.getByAttr;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerProvider(sdk).build();
  private final Tracer tracer = TraceShim.createTracerShim(sdk, new CorrelationContextManagerSdk());
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

        assertThat(inMemoryTracing.getSpanExporter().getFinishedSpanItems().size()).isEqualTo(0);
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

      List<SpanData> finished = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
      assertThat(finished.size()).isEqualTo(4);

      String component = Tags.COMPONENT.getKey();
      List<SpanData> spanExamplePromise = getByAttr(finished, component, "example-promises");
      assertThat(spanExamplePromise).hasSize(1);
      assertThat(spanExamplePromise.get(0).getParentSpanId()).isEqualTo(SpanId.getInvalid());

      assertThat(getByAttr(finished, component, "success")).hasSize(2);

      SpanId parentId = spanExamplePromise.get(0).getSpanId();
      for (SpanData span : getByAttr(finished, component, "success")) {
        assertThat(span.getParentSpanId()).isEqualTo(parentId);
      }

      List<SpanData> spanError = getByAttr(finished, component, "error");
      assertThat(spanError).hasSize(1);
      assertThat(spanError.get(0).getParentSpanId()).isEqualTo(parentId);
    }
  }
}
