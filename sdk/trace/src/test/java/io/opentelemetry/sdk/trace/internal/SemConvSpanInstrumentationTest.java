/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.semconv.incubating.OtelIncubatingAttributes;
import org.junit.jupiter.api.Test;

class SemConvSpanInstrumentationTest {

  @Test
  void verifyAttributesSemConvCompliant() {
    assertThat(SemConvSpanInstrumentation.getAttributesForSamplingDecisions(SamplingDecision.DROP))
        .hasSize(1)
        .containsEntry(
            OtelIncubatingAttributes.OTEL_SPAN_SAMPLING_RESULT,
            OtelIncubatingAttributes.OtelSpanSamplingResultIncubatingValues.DROP);

    assertThat(
            SemConvSpanInstrumentation.getAttributesForSamplingDecisions(
                SamplingDecision.RECORD_AND_SAMPLE))
        .hasSize(1)
        .containsEntry(
            OtelIncubatingAttributes.OTEL_SPAN_SAMPLING_RESULT,
            OtelIncubatingAttributes.OtelSpanSamplingResultIncubatingValues.RECORD_AND_SAMPLE);

    assertThat(
            SemConvSpanInstrumentation.getAttributesForSamplingDecisions(
                SamplingDecision.RECORD_ONLY))
        .hasSize(1)
        .containsEntry(
            OtelIncubatingAttributes.OTEL_SPAN_SAMPLING_RESULT,
            OtelIncubatingAttributes.OtelSpanSamplingResultIncubatingValues.RECORD_ONLY);
  }
}
