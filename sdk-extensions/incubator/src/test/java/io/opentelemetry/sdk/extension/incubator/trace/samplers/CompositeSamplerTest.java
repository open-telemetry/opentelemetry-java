/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_RANDOM_VALUE;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidRandomValue;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidThreshold;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class CompositeSamplerTest {
  private static class Input {
    private static final String traceId = "00112233445566778800000000000000";
    private static final String spanId = "0123456789abcdef";
    private static final String name = "name";
    private static final SpanKind spanKind = SpanKind.SERVER;
    private static final Attributes attributes = Attributes.empty();
    private static final List<LinkData> parentLinks = Collections.emptyList();
    private boolean parentSampled = true;

    private OptionalLong parentThreshold = OptionalLong.empty();
    private OptionalLong parentRandomValue = OptionalLong.empty();

    void setParentSampled(boolean parentSampled) {
      this.parentSampled = parentSampled;
    }

    void setParentThreshold(long parentThreshold) {
      assertThat(parentThreshold).isBetween(0L, 0xffffffffffffffL);
      this.parentThreshold = OptionalLong.of(parentThreshold);
    }

    void setParentRandomValue(long parentRandomValue) {
      assertThat(parentRandomValue).isBetween(0L, 0xffffffffffffffL);
      this.parentRandomValue = OptionalLong.of(parentRandomValue);
    }

    Context getParentContext() {
      return createParentContext(
          traceId, spanId, parentThreshold, parentRandomValue, parentSampled);
    }

    static String getTraceId() {
      return traceId;
    }

    static String getName() {
      return name;
    }

    static SpanKind getSpanKind() {
      return spanKind;
    }

    static Attributes getAttributes() {
      return attributes;
    }

    static List<LinkData> getParentLinks() {
      return parentLinks;
    }
  }

  private static class Output {

    private final SamplingResult samplingResult;
    private final Context parentContext;

    Output(SamplingResult samplingResult, Context parentContext) {
      this.samplingResult = samplingResult;
      this.parentContext = parentContext;
    }

    OptionalLong getThreshold() {
      Span parentSpan = Span.fromContext(parentContext);
      OtelTraceState otelTraceState =
          OtelTraceState.parse(
              samplingResult.getUpdatedTraceState(parentSpan.getSpanContext().getTraceState()));
      return isValidThreshold(otelTraceState.getThreshold())
          ? OptionalLong.of(otelTraceState.getThreshold())
          : OptionalLong.empty();
    }

    OptionalLong getRandomValue() {
      Span parentSpan = Span.fromContext(parentContext);
      OtelTraceState otelTraceState =
          OtelTraceState.parse(
              samplingResult.getUpdatedTraceState(parentSpan.getSpanContext().getTraceState()));
      return isValidRandomValue(otelTraceState.getRandomValue())
          ? OptionalLong.of(otelTraceState.getRandomValue())
          : OptionalLong.empty();
    }
  }

  private static TraceState createTraceState(OptionalLong threshold, OptionalLong randomValue) {
    long t = threshold.orElse(INVALID_THRESHOLD);
    long rv = randomValue.orElse(INVALID_RANDOM_VALUE);
    OtelTraceState state = new OtelTraceState(rv, t, Collections.emptyList());
    return TraceState.builder().put("ot", state.serialize()).build();
  }

  private static Context createParentContext(
      String traceId,
      String spanId,
      OptionalLong threshold,
      OptionalLong randomValue,
      boolean sampled) {
    TraceState parentTraceState = createTraceState(threshold, randomValue);
    TraceFlags traceFlags = sampled ? TraceFlags.getSampled() : TraceFlags.getDefault();
    SpanContext parentSpanContext =
        SpanContext.create(traceId, spanId, traceFlags, parentTraceState);
    Span parentSpan = Span.wrap(parentSpanContext);
    return parentSpan.storeInContext(Context.root());
  }

  private static Output sample(Input input, Sampler sampler) {
    Context parentContext = input.getParentContext();
    SamplingResult samplingResult =
        sampler.shouldSample(
            parentContext,
            Input.getTraceId(),
            Input.getName(),
            Input.getSpanKind(),
            Input.getAttributes(),
            Input.getParentLinks());
    return new Output(samplingResult, parentContext);
  }

  @Test
  void description() {
    assertThat(
            CompositeSampler.wrap(ComposableSampler.parentThreshold(ComposableSampler.alwaysOn()))
                .getDescription())
        .isEqualTo("ComposableParentThresholdSampler{rootSampler=ComposableAlwaysOnSampler}");
    assertThat(
            CompositeSampler.wrap(ComposableSampler.parentThreshold(ComposableSampler.alwaysOn())))
        .hasToString("ComposableParentThresholdSampler{rootSampler=ComposableAlwaysOnSampler}");
  }

  @Test
  void testMinThresholdWithoutParentRandomValue() {
    Input input = new Input();

    Sampler sampler = CompositeSampler.wrap(ComposableSampler.alwaysOn());

    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(output.getThreshold()).hasValue(0);
    assertThat(output.getRandomValue()).isNotPresent();
  }

  @Test
  void testMinThresholdWithParentRandomValue() {
    long parentRandomValue = 0x7f99aa40c02744L;

    Input input = new Input();
    input.setParentRandomValue(parentRandomValue);

    Sampler sampler = CompositeSampler.wrap(ComposableSampler.alwaysOn());

    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(output.getThreshold()).hasValue(0);
    assertThat(output.getRandomValue()).hasValue(parentRandomValue);
  }

  @Test
  void testMaxThreshold() {
    Input input = new Input();

    Sampler sampler = CompositeSampler.wrap(ComposableSampler.traceIdRatioBased(0.0));

    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.DROP);
    assertThat(output.getThreshold()).isNotPresent();
    assertThat(output.getRandomValue()).isNotPresent();
  }

  @Test
  void testParentBasedInConsistentMode() {
    long parentRandomValue = 0x7f99aa40c02744L;

    Input input = new Input();
    input.setParentRandomValue(parentRandomValue);
    input.setParentThreshold(parentRandomValue);
    input.setParentSampled(false); // should be ignored

    Sampler sampler =
        CompositeSampler.wrap(ComposableSampler.parentThreshold(ComposableSampler.alwaysOn()));

    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(output.getThreshold()).hasValue(parentRandomValue);
    assertThat(output.getRandomValue()).hasValue(parentRandomValue);
  }

  @Test
  void testParentBasedInLegacyMode() {
    // No parent threshold present
    Input input = new Input();

    Sampler sampler =
        CompositeSampler.wrap(ComposableSampler.parentThreshold(ComposableSampler.alwaysOn()));

    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(output.getThreshold()).isNotPresent();
    assertThat(output.getRandomValue()).isNotPresent();
  }

  @Test
  void testHalfThresholdNotSampled() {
    Input input = new Input();
    input.setParentRandomValue(0x7FFFFFFFFFFFFFL);

    Sampler sampler = CompositeSampler.wrap(ComposableSampler.traceIdRatioBased(0.5));

    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.DROP);
    assertThat(output.getThreshold()).isNotPresent();
    assertThat(output.getRandomValue()).hasValue(0x7FFFFFFFFFFFFFL);
  }

  @Test
  void testHalfThresholdSampled() {
    Input input = new Input();
    input.setParentRandomValue(0x80000000000000L);

    Sampler sampler = CompositeSampler.wrap(ComposableSampler.traceIdRatioBased(0.5));

    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(output.getThreshold()).hasValue(0x80000000000000L);
    assertThat(output.getRandomValue()).hasValue(0x80000000000000L);
  }

  @Test
  void testParentViolatingInvariant() {

    Input input = new Input();
    input.setParentThreshold(0x80000000000000L);
    input.setParentRandomValue(0x80000000000000L);
    input.setParentSampled(false);

    Sampler sampler = CompositeSampler.wrap(ComposableSampler.traceIdRatioBased(1.0));
    Output output = sample(input, sampler);

    assertThat(output.samplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(output.getThreshold()).hasValue(0x0L);
    assertThat(output.getRandomValue()).hasValue(0x80000000000000L);
  }
}
