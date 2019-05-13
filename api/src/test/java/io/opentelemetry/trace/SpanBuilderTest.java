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

package io.opentelemetry.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.samplers.Samplers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Span.Builder}. */
@RunWith(JUnit4.class)
public class SpanBuilderTest {
  private final Tracer tracer = NoopTrace.newNoopTracer();

  @Test
  public void doNotCrash_NoopImplementation() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    spanBuilder.setRecordEvents(true);
    spanBuilder.setSampler(Samplers.alwaysSample());
    spanBuilder.setSpanKind(Kind.SERVER);
    spanBuilder.setParent(BlankSpan.INSTANCE);
    spanBuilder.setParent(BlankSpan.INSTANCE.getContext());
    assertThat(spanBuilder.startSpan()).isSameAs(BlankSpan.INSTANCE);
  }
}
