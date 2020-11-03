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

class OtelInBraveTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("animal");
  private static final Context CONTEXT_WITH_ANIMAL = Context.root().with(ANIMAL, "japan");

  private static final Tracing TRACING =
      Tracing.newBuilder().currentTraceContext(CurrentTraceContext.Default.create()).build();
  private static final TraceContext TRACE_CONTEXT =
      BraveContextStorageProvider.toBraveContext(
          TraceContext.newBuilder().traceId(1).spanId(1).build(), CONTEXT_WITH_ANIMAL);

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
      assertThat(Context.current().get(ANIMAL)).isEqualTo("japan");
      try (Scope ignored2 = Context.current().with(ANIMAL, "cat").makeCurrent()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
        TraceContext context2 =
            Tracing.current().currentTraceContext().get().toBuilder().addExtra("cheese").build();
        try (CurrentTraceContext.Scope ignored3 =
            TRACING.currentTraceContext().newScope(context2)) {
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
      assertThat(Context.current().get(ANIMAL)).isEqualTo("japan");
      try (Scope ignored2 = Context.current().with(ANIMAL, "cat").makeCurrent()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
        TraceContext context2 =
            Tracing.current().currentTraceContext().get().toBuilder().addExtra("cheese").build();
        try (CurrentTraceContext.Scope ignored3 =
            TRACING.currentTraceContext().newScope(context2)) {
          AtomicReference<Boolean> braveContainsCheese = new AtomicReference<>();
          AtomicReference<String> otelValue = new AtomicReference<>();
          Runnable runnable =
              () -> {
                TraceContext traceContext = Tracing.current().currentTraceContext().get();
                if (traceContext != null && traceContext.extra().contains("cheese")) {
                  braveContainsCheese.set(true);
                } else {
                  braveContainsCheese.set(false);
                }
                otelValue.set(Context.current().get(ANIMAL));
              };

          otherThread.submit(runnable).get();
          assertThat(braveContainsCheese).hasValue(false);
          assertThat(otelValue).hasValue(null);

          otherThread.submit(TRACING.currentTraceContext().wrap(runnable)).get();
          assertThat(braveContainsCheese).hasValue(true);
          assertThat(otelValue).hasValue("cat");
        }
      }
    }
  }

  @Test
  void otelWrap() throws Exception {
    try (CurrentTraceContext.Scope ignored =
        TRACING.currentTraceContext().newScope(TRACE_CONTEXT)) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("japan");
      try (Scope ignored2 = Context.current().with(ANIMAL, "cat").makeCurrent()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
        TraceContext context2 =
            Tracing.current().currentTraceContext().get().toBuilder().addExtra("cheese").build();
        try (CurrentTraceContext.Scope ignored3 =
            TRACING.currentTraceContext().newScope(context2)) {
          AtomicReference<Boolean> braveContainsCheese = new AtomicReference<>();
          AtomicReference<String> otelValue = new AtomicReference<>();
          Runnable runnable =
              () -> {
                TraceContext traceContext = Tracing.current().currentTraceContext().get();
                if (traceContext != null && traceContext.extra().contains("cheese")) {
                  braveContainsCheese.set(true);
                } else {
                  braveContainsCheese.set(false);
                }
                otelValue.set(Context.current().get(ANIMAL));
              };

          otherThread.submit(runnable).get();
          assertThat(braveContainsCheese).hasValue(false);
          assertThat(otelValue).hasValue(null);

          Runnable task = Context.current().wrap(runnable);
          otherThread.submit(task).get();
          assertThat(braveContainsCheese).hasValue(true);
          assertThat(otelValue).hasValue("cat");
        }
      }
    }
  }
}
