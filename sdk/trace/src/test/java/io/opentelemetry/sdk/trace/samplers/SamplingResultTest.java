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
    assertThat(SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE))
        .isSameAs(SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE));
    assertThat(SamplingResult.create(SamplingResult.Decision.DROP))
        .isSameAs(SamplingResult.create(SamplingResult.Decision.DROP));

    assertThat(SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE).getDecision())
        .isEqualTo(SamplingResult.Decision.RECORD_AND_SAMPLE);
    assertThat(
            SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE)
                .getAttributes()
                .isEmpty())
        .isTrue();
    assertThat(SamplingResult.create(SamplingResult.Decision.DROP).getDecision())
        .isEqualTo(SamplingResult.Decision.DROP);
    assertThat(SamplingResult.create(SamplingResult.Decision.DROP).getAttributes().isEmpty())
        .isTrue();
  }

  @Test
  void emptyAttributes() {
    assertThat(SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE, Attributes.empty()))
        .isSameAs(SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE));
    assertThat(SamplingResult.create(SamplingResult.Decision.DROP, Attributes.empty()))
        .isSameAs(SamplingResult.create(SamplingResult.Decision.DROP));
  }

  @Test
  void hasAttributes() {
    final Attributes attrs = Attributes.of(longKey("foo"), 42L, stringKey("bar"), "baz");
    final SamplingResult sampledSamplingResult =
        SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE, attrs);
    assertThat(sampledSamplingResult.getDecision())
        .isEqualTo(SamplingResult.Decision.RECORD_AND_SAMPLE);
    assertThat(sampledSamplingResult.getAttributes()).isEqualTo(attrs);

    final SamplingResult notSampledSamplingResult =
        SamplingResult.create(SamplingResult.Decision.DROP, attrs);
    assertThat(notSampledSamplingResult.getDecision()).isEqualTo(SamplingResult.Decision.DROP);
    assertThat(notSampledSamplingResult.getAttributes()).isEqualTo(attrs);
  }
}
