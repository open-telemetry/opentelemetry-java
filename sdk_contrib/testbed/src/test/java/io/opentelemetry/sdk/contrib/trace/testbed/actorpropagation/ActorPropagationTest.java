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

package io.opentelemetry.sdk.contrib.trace.testbed.actorpropagation;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.contrib.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 *
 * <p>For improved readability, ignore the phaser lines as those are there to ensure deterministic
 * execution for the tests without sleeps.
 */
@SuppressWarnings("FutureReturnValueIgnored")
public class ActorPropagationTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
  private final Tracer tracer = TestUtils.createTracerShim(exporter);
  private Phaser phaser;

  @Before
  public void before() {
    phaser = new Phaser();
  }

  @Test
  public void testActorTell() {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      Span parent = tracer.spanBuilder("actorTell").setSpanKind(Kind.PRODUCER).startSpan();
      parent.setAttribute("component", "example-actor");
      try (Scope ignored = tracer.withSpan(parent)) {
        actor.tell("my message 1");
        actor.tell("my message 2");
      } finally {
        parent.end();
      }

      phaser.arriveAndAwaitAdvance(); // child tracer started
      assertThat(exporter.getFinishedSpanItems()).hasSize(1);
      phaser.arriveAndAwaitAdvance(); // continue...
      phaser.arriveAndAwaitAdvance(); // child tracer finished
      assertThat(exporter.getFinishedSpanItems()).hasSize(3);
      assertThat(TestUtils.getByKind(exporter.getFinishedSpanItems(), Span.Kind.CONSUMER))
          .hasSize(2);
      phaser.arriveAndDeregister(); // continue...

      List<SpanData> finished = exporter.getFinishedSpanItems();
      assertThat(finished.size()).isEqualTo(3);
      assertThat(finished.get(0).getContext().getTraceId())
          .isEqualTo(finished.get(1).getContext().getTraceId());
      assertThat(TestUtils.getByKind(finished, Span.Kind.CONSUMER)).hasSize(2);
      assertThat(TestUtils.getOneByKind(finished, Span.Kind.PRODUCER)).isNotNull();

      assertThat(tracer.getCurrentSpan()).isSameInstanceAs(DefaultSpan.getInvalid());
    }
  }

  @Test
  public void testActorAsk() throws ExecutionException, InterruptedException {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      Future<String> future1;
      Future<String> future2;
      Span span = tracer.spanBuilder("actorAsk").setSpanKind(Kind.PRODUCER).startSpan();
      span.setAttribute("component", "example-actor");

      try (Scope ignored = tracer.withSpan(span)) {
        future1 = actor.ask("my message 1");
        future2 = actor.ask("my message 2");
      } finally {
        span.end();
      }
      phaser.arriveAndAwaitAdvance(); // child tracer started
      assertThat(exporter.getFinishedSpanItems().size()).isEqualTo(1);
      phaser.arriveAndAwaitAdvance(); // continue...
      phaser.arriveAndAwaitAdvance(); // child tracer finished
      assertThat(exporter.getFinishedSpanItems().size()).isEqualTo(3);
      assertThat(TestUtils.getByKind(exporter.getFinishedSpanItems(), Span.Kind.CONSUMER))
          .hasSize(2);
      phaser.arriveAndDeregister(); // continue...

      List<SpanData> finished = exporter.getFinishedSpanItems();
      String message1 = future1.get(); // This really should be a non-blocking callback...
      String message2 = future2.get(); // This really should be a non-blocking callback...
      assertThat(message1).isEqualTo("received my message 1");
      assertThat(message2).isEqualTo("received my message 2");

      assertThat(finished.size()).isEqualTo(3);
      assertThat(finished.get(0).getTraceId()).isEqualTo(finished.get(1).getTraceId());
      assertThat(TestUtils.getByKind(finished, Span.Kind.CONSUMER)).hasSize(2);
      assertThat(TestUtils.getOneByKind(finished, Span.Kind.PRODUCER)).isNotNull();

      assertThat(tracer.getCurrentSpan()).isSameInstanceAs(DefaultSpan.getInvalid());
    }
  }
}
