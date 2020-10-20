/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;

import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OtelAsBraveTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("animal");

  private static final Tracing TRACING =
      Tracing.newBuilder().currentTraceContext(CurrentTraceContext.Default.create()).build();

  private static final TraceContext TRACE_CONTEXT =
      TraceContext.newBuilder().traceId(1).spanId(1).addExtra("japan").build();

  private static ExecutorService otherThread;

  @BeforeAll
  static void setUp() {
    otherThread = Executors.newSingleThreadExecutor();
  }

  @AfterAll
  static void tearDown() {
    otherThread.shutdown();
  }

  @Test
  void braveOtelMix() {
    try (CurrentTraceContext.Scope ignored =
        TRACING.currentTraceContext().newScope(TRACE_CONTEXT)) {
      assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
      try (Scope ignored2 = Context.current().with(ANIMAL, "cat").makeCurrent()) {
        assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");

        TraceContext context2 =
            Tracing.current().currentTraceContext().get().toBuilder().addExtra("cheese").build();
        try (CurrentTraceContext.Scope ignored3 =
            TRACING.currentTraceContext().newScope(context2)) {
          assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
          assertThat(Tracing.current().currentTraceContext().get().extra()).contains("cheese");
          assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
        }
      }
    }
  }

  @Test
  void braveWrap() throws Exception {
    try (CurrentTraceContext.Scope ignored =
        TRACING.currentTraceContext().newScope(TRACE_CONTEXT)) {
      try (Scope ignored2 = Context.current().with(ANIMAL, "cat").makeCurrent()) {
        assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
        AtomicReference<Boolean> braveContainsJapan = new AtomicReference<>();
        AtomicReference<String> otelValue = new AtomicReference<>();
        Runnable runnable =
            () -> {
              TraceContext traceContext = Tracing.current().currentTraceContext().get();
              if (traceContext != null && traceContext.extra().contains("japan")) {
                braveContainsJapan.set(true);
              } else {
                braveContainsJapan.set(false);
              }
              otelValue.set(Context.current().get(ANIMAL));
            };
        otherThread.submit(runnable).get();
        assertThat(braveContainsJapan).hasValue(false);
        assertThat(otelValue).hasValue(null);

        otherThread.submit(TRACING.currentTraceContext().wrap(runnable)).get();
        assertThat(braveContainsJapan).hasValue(true);
        assertThat(otelValue).hasValue("cat");
      }
    }
  }

  @Test
  void otelWrap() throws Exception {
    try (CurrentTraceContext.Scope ignored =
        TRACING.currentTraceContext().newScope(TRACE_CONTEXT)) {
      try (Scope ignored2 = Context.current().with(ANIMAL, "cat").makeCurrent()) {
        assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
        AtomicReference<Boolean> braveContainsJapan = new AtomicReference<>(false);
        AtomicReference<String> otelValue = new AtomicReference<>();
        Runnable runnable =
            () -> {
              TraceContext traceContext = Tracing.current().currentTraceContext().get();
              if (traceContext != null && traceContext.extra().contains("japan")) {
                braveContainsJapan.set(true);
              } else {
                braveContainsJapan.set(false);
              }
              otelValue.set(Context.current().get(ANIMAL));
            };
        otherThread.submit(runnable).get();
        assertThat(braveContainsJapan).hasValue(false);
        assertThat(otelValue).hasValue(null);

        otherThread.submit(Context.current().wrap(runnable)).get();
        assertThat(braveContainsJapan).hasValue(true);
        assertThat(otelValue).hasValue("cat");
      }
    }
  }
}
