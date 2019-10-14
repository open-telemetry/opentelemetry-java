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

package io.opentelemetry.sdk.trace;

import com.google.common.truth.Truth;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Samplers}. */
@RunWith(JUnit4.class)
public class SamplersTest {
  private final TraceId traceId = TestUtils.generateRandomTraceId();
  private final SpanId parentSpanId = TestUtils.generateRandomSpanId();
  private final SpanId spanId = TestUtils.generateRandomSpanId();
  private final Tracestate tracestate = Tracestate.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          traceId, parentSpanId, TraceFlags.builder().setIsSampled(true).build(), tracestate);
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), tracestate);

  @Test
  public void alwaysOnSampler_AlwaysReturnTrue() {
    // Sampled parent.
    Truth.assertThat(
            Samplers.alwaysOn()
                .shouldSample(
                    sampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "Another name",
                    Collections.<Link>emptyList())
                .isSampled())
        .isTrue();
    // Not sampled parent.
    Truth.assertThat(
            Samplers.alwaysOn()
                .shouldSample(
                    notSampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "Yet another name",
                    Collections.<Link>emptyList())
                .isSampled())
        .isTrue();
  }

  @Test
  public void alwaysOnSampler_ToString() {
    Truth.assertThat(Samplers.alwaysOn().toString()).isEqualTo("AlwaysOnSampler");
  }

  @Test
  public void alwaysOffSampler_AlwaysReturnFalse() {
    // Sampled parent.
    Truth.assertThat(
            Samplers.alwaysOff()
                .shouldSample(
                    sampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "bar",
                    Collections.<Link>emptyList())
                .isSampled())
        .isFalse();
    // Not sampled parent.
    Truth.assertThat(
            Samplers.alwaysOff()
                .shouldSample(
                    notSampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "quux",
                    Collections.<Link>emptyList())
                .isSampled())
        .isFalse();
  }

  @Test
  public void alwaysOffSampler_ToString() {
    Truth.assertThat(Samplers.alwaysOff().toString()).isEqualTo("AlwaysOffSampler");
  }
}
