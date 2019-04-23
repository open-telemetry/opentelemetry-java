/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.Callable;
import openconsensus.trace.Span.Kind;
import openconsensus.trace.samplers.Samplers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpanBuilder}. */
@RunWith(JUnit4.class)
// Need to suppress warnings for MustBeClosed because Java-6 does not support try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public class SpanBuilderTest {
  private final Tracer tracer = Trace.getTracer();
  private final SpanBuilder spanBuilder = tracer.spanBuilder("test");

  @Test
  public void startSpanAndRun() {
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    spanBuilder.startSpanAndRun(
        new Runnable() {
          @Override
          public void run() {
            assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
          }
        });
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void startSpanAndCall() throws Exception {
    final Object ret = new Object();
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    assertThat(
            spanBuilder.startSpanAndCall(
                new Callable<Object>() {
                  @Override
                  public Object call() throws Exception {
                    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
                    return ret;
                  }
                }))
        .isEqualTo(ret);
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void doNotCrash_NoopImplementation() throws Exception {
    SpanBuilder spanBuilder = tracer.spanBuilder("MySpanName");
    spanBuilder.setRecordEvents(true);
    spanBuilder.setSampler(Samplers.alwaysSample());
    spanBuilder.setSpanKind(Kind.SERVER);
    assertThat(spanBuilder.startSpan()).isSameAs(BlankSpan.INSTANCE);
  }
}
