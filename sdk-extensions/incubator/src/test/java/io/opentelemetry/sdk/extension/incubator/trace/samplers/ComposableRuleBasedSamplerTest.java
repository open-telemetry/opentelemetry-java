/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ComposableRuleBasedSamplerTest {

  private static final AttributeKey<String> HTTP_ROUTE = AttributeKey.stringKey("http.route");

  private static final class AttributePredicate<T> implements SamplingPredicate {

    private final AttributeKey<T> key;
    private final String description;

    private AttributePredicate(AttributeKey<T> key, T value) {
      this.key = key;
      this.description = key.getKey() + "=" + value;
    }

    @Override
    public boolean matches(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {
      return "/health".equals(attributes.get(key));
    }

    @Override
    public String toString() {
      return description;
    }
  }

  private enum IsRootPredicate implements SamplingPredicate {
    INSTANCE;

    @Override
    public boolean matches(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {
      return !Span.fromContext(parentContext).getSpanContext().isValid();
    }

    @Override
    public String toString() {
      return "isRoot";
    }
  }

  @Test
  void testDescription() {
    assertThat(ComposableSampler.ruleBasedBuilder().build().getDescription())
        .isEqualTo("ComposableRuleBasedSampler{[]}");
    assertThat(
            ComposableSampler.ruleBasedBuilder()
                .add(new AttributePredicate<>(HTTP_ROUTE, "/health"), ComposableSampler.alwaysOff())
                .build()
                .getDescription())
        .isEqualTo("ComposableRuleBasedSampler{[(http.route=/health:ComposableAlwaysOffSampler)]}");
    assertThat(
            ComposableSampler.ruleBasedBuilder()
                .add(new AttributePredicate<>(HTTP_ROUTE, "/health"), ComposableSampler.alwaysOff())
                .add(IsRootPredicate.INSTANCE, ComposableSampler.alwaysOn())
                .build()
                .getDescription())
        .isEqualTo(
            "ComposableRuleBasedSampler{[(http.route=/health:ComposableAlwaysOffSampler),(isRoot:ComposableAlwaysOnSampler)]}");
  }

  @Test
  void noRules() {
    Sampler sampler = CompositeSampler.wrap(ComposableSampler.ruleBasedBuilder().build());
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TraceId.fromLongs(1, 2),
                    SpanId.fromLong(3),
                    SpanKind.SERVER,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.DROP);
  }

  @Test
  void rules() {
    Sampler sampler =
        CompositeSampler.wrap(
            ComposableSampler.ruleBasedBuilder()
                .add(new AttributePredicate<>(HTTP_ROUTE, "/health"), ComposableSampler.alwaysOff())
                .add(IsRootPredicate.INSTANCE, ComposableSampler.alwaysOn())
                .build());

    // root health check
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TraceId.fromLongs(1, 2),
                    SpanId.fromLong(3),
                    SpanKind.SERVER,
                    Attributes.of(HTTP_ROUTE, "/health"),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.DROP);

    // root
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TraceId.fromLongs(1, 2),
                    SpanId.fromLong(3),
                    SpanKind.SERVER,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);

    // no match
    assertThat(
            sampler
                .shouldSample(
                    Context.root()
                        .with(
                            Span.wrap(
                                SpanContext.create(
                                    TraceId.fromLongs(1, 2),
                                    SpanId.fromLong(2),
                                    TraceFlags.getSampled(),
                                    TraceState.getDefault()))),
                    TraceId.fromLongs(1, 2),
                    SpanId.fromLong(3),
                    SpanKind.SERVER,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.DROP);
  }
}
