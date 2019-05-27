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

package io.opentelemetry.trace.samplers;

import static io.opentelemetry.trace.TestUtils.generateRandomSpanId;
import static io.opentelemetry.trace.TestUtils.generateRandomTraceId;

import com.google.common.truth.Truth;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.Collections;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Samplers}. */
@RunWith(JUnit4.class)
public class SamplersTest {
  private final Random random = new Random(1234);
  private final TraceId traceId = generateRandomTraceId(random);
  private final SpanId parentSpanId = generateRandomSpanId(random);
  private final SpanId spanId = generateRandomSpanId(random);
  private final Tracestate tracestate = Tracestate.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          traceId, parentSpanId, TraceOptions.builder().setIsSampled(true).build(), tracestate);
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceOptions.DEFAULT, tracestate);

  @Test
  public void alwaysSampleSampler_AlwaysReturnTrue() {
    // Sampled parent.
    Truth.assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    sampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "Another name",
                    Collections.<Span>emptyList())
                .isSampled())
        .isTrue();
    // Not sampled parent.
    Truth.assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    notSampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "Yet another name",
                    Collections.<Span>emptyList())
                .isSampled())
        .isTrue();
  }

  @Test
  public void alwaysSampleSampler_ToString() {
    Truth.assertThat(Samplers.alwaysSample().toString()).isEqualTo("AlwaysSampleSampler");
  }

  @Test
  public void neverSampleSampler_AlwaysReturnFalse() {
    // Sampled parent.
    Truth.assertThat(
            Samplers.neverSample()
                .shouldSample(
                    sampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "bar",
                    Collections.<Span>emptyList())
                .isSampled())
        .isFalse();
    // Not sampled parent.
    Truth.assertThat(
            Samplers.neverSample()
                .shouldSample(
                    notSampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "quux",
                    Collections.<Span>emptyList())
                .isSampled())
        .isFalse();
  }

  @Test
  public void neverSampleSampler_ToString() {
    Truth.assertThat(Samplers.neverSample().toString()).isEqualTo("NeverSampleSampler");
  }
}
