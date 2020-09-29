/*
 * Copyright 2020, OpenTelemetry Authors
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
      try (Scope ignored2 = Context.current().withValues(ANIMAL, "cat").makeCurrent()) {
        assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
        assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");

        TraceContext context2 =
            Tracing.current().currentTraceContext().get().toBuilder().addExtra("cheese").build();
        try (CurrentTraceContext.Scope ignored3 =
            TRACING.currentTraceContext().newScope(context2)) {
          assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
          assertThat(Tracing.current().currentTraceContext().get().extra()).contains("cheese");
          assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");
        }
      }
    }
  }

  @Test
  void braveWrap() throws Exception {
    try (CurrentTraceContext.Scope ignored =
        TRACING.currentTraceContext().newScope(TRACE_CONTEXT)) {
      try (Scope ignored2 = Context.current().withValues(ANIMAL, "cat").makeCurrent()) {
        assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
        assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");
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
              otelValue.set(Context.current().getValue(ANIMAL));
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
      try (Scope ignored2 = Context.current().withValues(ANIMAL, "cat").makeCurrent()) {
        assertThat(Tracing.current().currentTraceContext().get().extra()).contains("japan");
        assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");
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
              otelValue.set(Context.current().getValue(ANIMAL));
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
