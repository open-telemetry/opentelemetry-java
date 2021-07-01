/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

class SamplingResultTest {

  @Test
  void noAttributes() {
    assertThat(SamplingResult.recordAndSample()).isSameAs(SamplingResult.recordAndSample());
    assertThat(SamplingResult.drop()).isSameAs(SamplingResult.drop());

    assertThat(SamplingResult.recordAndSample().getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(SamplingResult.recordAndSample().getAttributes().isEmpty())
        .isTrue();
    assertThat(SamplingResult.drop().getDecision())
        .isEqualTo(SamplingDecision.DROP);
    assertThat(SamplingResult.drop().getAttributes().isEmpty())
        .isTrue();
  }

  @Test
  void emptyAttributes() {
    assertThat(SamplingResult.recordAndSample(Attributes.empty()))
        .isSameAs(SamplingResult.recordAndSample());
    assertThat(SamplingResult.drop(Attributes.empty()))
        .isSameAs(SamplingResult.drop());
  }

  @Test
  void hasAttributes() {
    final Attributes attrs = Attributes.of(longKey("foo"), 42L, stringKey("bar"), "baz");
    final SamplingResult sampledSamplingResult =
        SamplingResult.recordAndSample(attrs);
    assertThat(sampledSamplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(sampledSamplingResult.getAttributes()).isEqualTo(attrs);

    final SamplingResult notSampledSamplingResult =
        SamplingResult.drop(attrs);
    assertThat(notSampledSamplingResult.getDecision()).isEqualTo(SamplingDecision.DROP);
    assertThat(notSampledSamplingResult.getAttributes()).isEqualTo(attrs);
  }
}
