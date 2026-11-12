/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ContextScopedAttributesSpanTest {

  private InMemorySpanExporter exporter;
  private SdkTracerProviderBuilder builder;

  @BeforeEach
  void setUp() {
    exporter = InMemorySpanExporter.create();
    builder = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter));
  }

  @Test
  void disabledByDefault() {
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context ctx = ContextAttributes.set(Context.current(), Attributes.of(stringKey("k1"), "v1"));
    try (Scope ignored = ctx.makeCurrent()) {
      tracer.spanBuilder("span").startSpan().end();
    }

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(span -> assertThat(span).hasAttributes(Attributes.empty()));
  }

  @Test
  void enabled_mergesBeforeSamplerWithExplicitPrecedence() {
    Sampler sampler = mock(Sampler.class);
    when(sampler.shouldSample(any(), any(), any(), any(), any(), any()))
        .thenReturn(SamplingResult.recordAndSample());

    builder.setSampler(sampler);
    SdkTracerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(stringKey("k1"), "from-context", stringKey("k2"), "from-context"));
    try (Scope ignored = ctx.makeCurrent()) {
      tracer
          .spanBuilder("span")
          .setAttribute("k2", "explicit")
          .setAttribute("k3", "v3")
          .startSpan()
          .end();
    }

    ArgumentCaptor<Attributes> sampledAttributes = ArgumentCaptor.forClass(Attributes.class);
    verify(sampler).shouldSample(any(), any(), any(), any(), sampledAttributes.capture(), any());

    Attributes expected =
        Attributes.of(
            stringKey("k1"), "from-context", stringKey("k2"), "explicit", stringKey("k3"), "v3");
    assertThat(sampledAttributes.getValue().asMap()).isEqualTo(expected.asMap());

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(span -> assertThat(span).hasAttributes(expected));
  }

  @Test
  void usesExplicitParentContext() {
    SdkTracerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context explicitParent =
        ContextAttributes.set(Context.root(), Attributes.of(stringKey("parent"), "explicit"));
    Context currentCtx =
        ContextAttributes.set(Context.root(), Attributes.of(stringKey("parent"), "current"));

    try (Scope ignored = currentCtx.makeCurrent()) {
      tracer.spanBuilder("span").setParent(explicitParent).startSpan().end();
    }

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(
            span -> assertThat(span).hasAttributes(attributeEntry("parent", "explicit")));
  }

  @Test
  void explicitParentRoot_dropsContextAttributes() {
    SdkTracerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context currentCtx =
        ContextAttributes.set(Context.root(), Attributes.of(stringKey("scope"), "current"));

    try (Scope ignored = currentCtx.makeCurrent()) {
      tracer.spanBuilder("span").setParent(Context.root()).startSpan().end();
    }

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(span -> {
          assertThat(span).hasAttributes(Attributes.empty());
          assertThat(span.getParentSpanContext().isValid()).isFalse();
        });
  }

  @Test
  void spanAttributeCountLimit() {
    builder.setSpanLimits(SpanLimits.builder().setMaxNumberOfAttributes(1).build());
    SdkTracerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context ctx =
        ContextAttributes.set(Context.root(), Attributes.of(stringKey("ctx_attr"), "ctx"));

    try (Scope ignored = ctx.makeCurrent()) {
      tracer.spanBuilder("span").setAttribute("explicit_attr", "explicit").startSpan().end();
    }

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(span ->
            assertThat(span)
                .hasAttributes(attributeEntry("explicit_attr", "explicit"))
                .hasTotalAttributeCount(2));
  }

  @Test
  void setNoParent_isolatesTraceAndBaggageWhilePreservingContextAttributes() {
    Sampler sampler = mock(Sampler.class);
    when(sampler.shouldSample(any(), any(), any(), any(), any(), any()))
        .thenReturn(SamplingResult.recordAndSample());

    SpanProcessor processor = mock(SpanProcessor.class);
    when(processor.isStartRequired()).thenReturn(true);

    builder.setSampler(sampler).addSpanProcessor(processor);
    SdkTracerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope parentScope = parent.makeCurrent()) {
      Context currentCtx =
          ContextAttributes.set(Context.current(), Attributes.of(stringKey("tenant.id"), "acme"))
              .with(Baggage.builder().put("baggage.key", "baggage.value").build());

      try (Scope ignored = currentCtx.makeCurrent()) {
        tracer.spanBuilder("span").setNoParent().startSpan().end();
      }
    } finally {
      parent.end();
    }

    ArgumentCaptor<Context> samplerContextCaptor = ArgumentCaptor.forClass(Context.class);
    ArgumentCaptor<Attributes> samplerAttributesCaptor = ArgumentCaptor.forClass(Attributes.class);
    verify(sampler, times(2))
        .shouldSample(
            samplerContextCaptor.capture(),
            any(),
            any(),
            any(),
            samplerAttributesCaptor.capture(),
            any());

    Context capturedSamplerCtx = samplerContextCaptor.getAllValues().get(1);
    assertThat(Baggage.fromContext(capturedSamplerCtx).isEmpty()).isTrue();
    assertThat(Span.fromContext(capturedSamplerCtx).getSpanContext().isValid()).isFalse();
    assertThat(samplerAttributesCaptor.getAllValues().get(1).asMap())
        .isEqualTo(Attributes.of(stringKey("tenant.id"), "acme").asMap());

    ArgumentCaptor<Context> processorContextCaptor = ArgumentCaptor.forClass(Context.class);
    verify(processor, times(2)).onStart(processorContextCaptor.capture(), any());
    Context capturedProcessorCtx = processorContextCaptor.getAllValues().get(1);
    assertThat(Baggage.fromContext(capturedProcessorCtx).isEmpty()).isTrue();
    assertThat(Span.fromContext(capturedProcessorCtx).getSpanContext().isValid()).isFalse();

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(
            child -> {
              assertThat(child).hasAttributes(attributeEntry("tenant.id", "acme"));
              assertThat(child.getParentSpanContext().isValid()).isFalse();
              assertThat(child.getSpanContext().getTraceId())
                  .isNotEqualTo(parent.getSpanContext().getTraceId());
            },
            parentSpan -> assertThat(parentSpan.getSpanContext().isValid()).isTrue());
  }

  @Test
  void sameNameDifferentType_explicitWins() {
    SdkTracerProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context ctx =
        ContextAttributes.set(
            Context.current(), Attributes.of(stringKey("collision"), "contextString"));
    try (Scope ignored = ctx.makeCurrent()) {
      tracer.spanBuilder("span").setAttribute(longKey("collision"), 12345L).startSpan().end();
    }

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(
            span -> assertThat(span).hasAttributes(attributeEntry("collision", 12345L)));
  }

  @Test
  void filterPredicate_filtersByScopeAndKey() {
    SdkTracerProviderUtil.setContextScopedAttributesFilter(
        builder,
        (scope, key) ->
            scope.getName().equals("allowed-scope") && key.getKey().startsWith("allowed."));
    SdkTracerProvider provider = builder.build();

    Tracer allowedTracer = provider.get("allowed-scope");
    Tracer otherTracer = provider.get("other-scope");

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(
                stringKey("allowed.key"), "val1",
                stringKey("denied.key"), "val2"));

    try (Scope ignored = ctx.makeCurrent()) {
      allowedTracer.spanBuilder("s1").startSpan().end();
      otherTracer.spanBuilder("s2").startSpan().end();
    }

    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactly(
            s1 -> assertThat(s1).hasAttributes(attributeEntry("allowed.key", "val1")),
            s2 -> assertThat(s2).hasAttributes(Attributes.empty()));
  }
}
