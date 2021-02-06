/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.actorpropagation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
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
@SuppressWarnings("FutureReturnValueIgnored")
class ActorPropagationTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(ActorPropagationTest.class.getName());
  private Phaser phaser;

  @BeforeEach
  void before() {
    phaser = new Phaser();
  }

  @Test
  void testActorTell() {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      Span parent = tracer.spanBuilder("actorTell").setSpanKind(SpanKind.PRODUCER).startSpan();
      parent.setAttribute("component", "example-actor");
      try (Scope ignored = parent.makeCurrent()) {
        actor.tell("my message 1");
        actor.tell("my message 2");
      } finally {
        parent.end();
      }

      phaser.arriveAndAwaitAdvance(); // child tracer started
      assertThat(otelTesting.getSpans()).hasSize(1);
      phaser.arriveAndAwaitAdvance(); // continue...
      phaser.arriveAndAwaitAdvance(); // child tracer finished
      assertThat(otelTesting.getSpans()).hasSize(3);
      assertThat(TestUtils.getByKind(otelTesting.getSpans(), SpanKind.CONSUMER)).hasSize(2);
      phaser.arriveAndDeregister(); // continue...

      List<SpanData> finished = otelTesting.getSpans();
      assertThat(finished.size()).isEqualTo(3);
      assertThat(finished.get(0).getTraceIdHex()).isEqualTo(finished.get(1).getTraceIdHex());
      assertThat(TestUtils.getByKind(finished, SpanKind.CONSUMER)).hasSize(2);
      assertThat(TestUtils.getOneByKind(finished, SpanKind.PRODUCER)).isNotNull();

      assertThat(Span.current()).isSameAs(Span.getInvalid());
    }
  }

  @Test
  void testActorAsk() throws ExecutionException, InterruptedException {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      Future<String> future1;
      Future<String> future2;
      Span span = tracer.spanBuilder("actorAsk").setSpanKind(SpanKind.PRODUCER).startSpan();
      span.setAttribute("component", "example-actor");

      try (Scope ignored = span.makeCurrent()) {
        future1 = actor.ask("my message 1");
        future2 = actor.ask("my message 2");
      } finally {
        span.end();
      }
      phaser.arriveAndAwaitAdvance(); // child tracer started
      assertThat(otelTesting.getSpans().size()).isEqualTo(1);
      phaser.arriveAndAwaitAdvance(); // continue...
      phaser.arriveAndAwaitAdvance(); // child tracer finished
      assertThat(otelTesting.getSpans().size()).isEqualTo(3);
      assertThat(TestUtils.getByKind(otelTesting.getSpans(), SpanKind.CONSUMER)).hasSize(2);
      phaser.arriveAndDeregister(); // continue...

      List<SpanData> finished = otelTesting.getSpans();
      String message1 = future1.get(); // This really should be a non-blocking callback...
      String message2 = future2.get(); // This really should be a non-blocking callback...
      assertThat(message1).isEqualTo("received my message 1");
      assertThat(message2).isEqualTo("received my message 2");

      assertThat(finished.size()).isEqualTo(3);
      assertThat(finished.get(0).getTraceIdHex()).isEqualTo(finished.get(1).getTraceIdHex());
      assertThat(TestUtils.getByKind(finished, SpanKind.CONSUMER)).hasSize(2);
      assertThat(TestUtils.getOneByKind(finished, SpanKind.PRODUCER)).isNotNull();

      assertThat(Span.current()).isSameAs(Span.getInvalid());
    }
  }
}
