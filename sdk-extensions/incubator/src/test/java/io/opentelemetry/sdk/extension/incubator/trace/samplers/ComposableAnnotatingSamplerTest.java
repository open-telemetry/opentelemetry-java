/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ComposableAnnotatingSamplerTest {

  private static final Attributes ATTRIBUTES =
      Attributes.of(
          AttributeKey.stringKey("http.route"), "/bear", AttributeKey.longKey("size"), 100L);

  @Test
  void testDescription() {
    assertThat(
            ComposableSampler.annotating(ComposableSampler.alwaysOn(), Attributes.empty())
                .getDescription())
        .isEqualTo("ComposableAnnotatingSampler{ComposableAlwaysOnSampler,{}}");
    assertThat(
            ComposableSampler.annotating(ComposableSampler.alwaysOn(), ATTRIBUTES).getDescription())
        .isEqualTo(
            "ComposableAnnotatingSampler{ComposableAlwaysOnSampler,{http.route=\"/bear\", size=100}}");
  }

  @Test
  void setsAttributes() {
    SamplingIntent intent =
        ComposableSampler.annotating(ComposableSampler.alwaysOn(), ATTRIBUTES)
            .getSamplingIntent(
                Context.root(),
                TraceId.getInvalid(),
                "span",
                SpanKind.SERVER,
                Attributes.empty(),
                Collections.emptyList());
    assertThat(intent.getThreshold()).isEqualTo(0);
    assertThat(intent.getAttributes()).isEqualTo(ATTRIBUTES);
  }

  @Test
  void mergesAttributes() {
    // Easiest way to have a SamplingIntent with attributes is to use annotating sampler itself.
    // This effectively creates a "test coverage dependency" where we know it's ok since we verify
    // the base case in setsAttributes().
    ComposableSampler baseSampler =
        ComposableSampler.annotating(
            ComposableSampler.alwaysOn(),
            Attributes.of(
                AttributeKey.stringKey("http.route"),
                "/cat",
                AttributeKey.stringKey("type"),
                "mammal"));

    SamplingIntent intent =
        ComposableSampler.annotating(baseSampler, ATTRIBUTES)
            .getSamplingIntent(
                Context.root(),
                TraceId.getInvalid(),
                "span",
                SpanKind.SERVER,
                Attributes.empty(),
                Collections.emptyList());
    assertThat(intent.getThreshold()).isEqualTo(0);
    assertThat(intent.getAttributes())
        .isEqualTo(
            Attributes.of(
                AttributeKey.stringKey("http.route"),
                "/bear",
                AttributeKey.longKey("size"),
                100L,
                AttributeKey.stringKey("type"),
                "mammal"));
  }
}
