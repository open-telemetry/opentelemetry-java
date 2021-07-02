/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RateLimitingSamplerTest {

  // RateLimiter is well tested, just do some sanity check.
  @Test
  void limitsRate() {
    TestClock clock = TestClock.create();
    RateLimitingSampler sampler = new RateLimitingSampler(1, clock);
    assertThat(sampler.getDescription()).isEqualTo("RateLimitingSampler{1}");

    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TraceId.fromLongs(1, 2),
                    "span",
                    SpanKind.CLIENT,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    // Balanced used up
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TraceId.fromLongs(1, 2),
                    "span",
                    SpanKind.CLIENT,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.DROP);

    clock.advance(Duration.ofMillis(100));
    // Balance restored after a second, not yet
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TraceId.fromLongs(1, 2),
                    "span",
                    SpanKind.CLIENT,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.DROP);
    clock.advance(Duration.ofMillis(900));
    // Balance restored
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TraceId.fromLongs(1, 2),
                    "span",
                    SpanKind.CLIENT,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }
}
