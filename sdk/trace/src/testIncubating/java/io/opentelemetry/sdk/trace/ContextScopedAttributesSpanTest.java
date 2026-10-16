/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ContextScopedAttributesSpanTest {

  @Test
  void contextScopedAttributes_disabledByDefault() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProvider provider =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
    Tracer tracer = provider.get("test");

    Context ctx = ContextAttributes.add(Context.current(), Attributes.of(stringKey("k1"), "v1"));
    try (Scope ignored = ctx.makeCurrent()) {
      Span span = tracer.spanBuilder("span").startSpan();
      span.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getAttributes()).isEmpty();
  }

  @Test
  void contextScopedAttributes_enabled_mergesBeforeSamplerWithExplicitPrecedence() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    AtomicReference<Attributes> sampledAttributes = new AtomicReference<>();
    Sampler inspectSampler =
        new Sampler() {
          @Override
          public SamplingResult shouldSample(
              Context parentContext,
              String traceId,
              String name,
              SpanKind spanKind,
              Attributes attributes,
              List<LinkData> parentLinks) {
            sampledAttributes.set(attributes);
            return SamplingResult.recordAndSample();
          }

          @Override
          public String getDescription() {
            return "inspectSampler";
          }
        };

    SdkTracerProviderBuilder builder =
        SdkTracerProvider.builder()
            .setSampler(inspectSampler)
            .addSpanProcessor(SimpleSpanProcessor.create(exporter));
    SdkTracerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(stringKey("k1"), "from-context", stringKey("k2"), "from-context"));
    try (Scope ignored = ctx.makeCurrent()) {
      Span span =
          tracer
              .spanBuilder("span")
              .setAttribute("k2", "explicit")
              .setAttribute("k3", "v3")
              .startSpan();
      span.end();
    }

    Attributes expected =
        Attributes.of(
            stringKey("k1"), "from-context", stringKey("k2"), "explicit", stringKey("k3"), "v3");
    assertThat(sampledAttributes.get().asMap()).isEqualTo(expected.asMap());

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0)).hasAttributes(expected);
  }

  @Test
  void contextScopedAttributes_usesExplicitParentContext() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProviderBuilder builder =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter));
    SdkTracerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context explicitParent =
        ContextAttributes.add(Context.root(), Attributes.of(stringKey("parent"), "explicit"));
    Context currentCtx =
        ContextAttributes.add(Context.root(), Attributes.of(stringKey("parent"), "current"));

    try (Scope ignored = currentCtx.makeCurrent()) {
      Span span = tracer.spanBuilder("span").setParent(explicitParent).startSpan();
      span.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0)).hasAttributes(Attributes.of(stringKey("parent"), "explicit"));
  }

  @Test
  void contextScopedAttributes_setNoParent_preservesAmbientContextAttributes() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProviderBuilder builder =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter));
    SdkTracerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope parentScope = parent.makeCurrent()) {
      Context currentCtx =
          ContextAttributes.add(Context.current(), Attributes.of(stringKey("scope"), "current"));

      try (Scope ignored = currentCtx.makeCurrent()) {
        Span span = tracer.spanBuilder("span").setNoParent().startSpan();
        span.end();
      }
    } finally {
      parent.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(2);
    SpanData child = spans.get(0);
    assertThat(child).hasAttributes(Attributes.of(stringKey("scope"), "current"));
    assertThat(child.getParentSpanContext().isValid()).isFalse();
    assertThat(child.getSpanContext().getTraceId())
        .isNotEqualTo(parent.getSpanContext().getTraceId());
  }

  @Test
  void contextScopedAttributes_explicitParentRoot_dropsContextAttributes() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProviderBuilder builder =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter));
    SdkTracerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context currentCtx =
        ContextAttributes.add(Context.root(), Attributes.of(stringKey("scope"), "current"));

    try (Scope ignored = currentCtx.makeCurrent()) {
      Span span = tracer.spanBuilder("span").setParent(Context.root()).startSpan();
      span.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    SpanData spanData = spans.get(0);
    assertThat(spanData.getAttributes()).isEmpty();
    assertThat(spanData.getParentSpanContext().isValid()).isFalse();
  }

  @Test
  void contextScopedAttributes_spanAttributeCountLimit() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProviderBuilder builder =
        SdkTracerProvider.builder()
            .setSpanLimits(SpanLimits.builder().setMaxNumberOfAttributes(1).build())
            .addSpanProcessor(SimpleSpanProcessor.create(exporter));
    SdkTracerProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkTracerProvider provider = builder.build();
    Tracer tracer = provider.get("test");

    Context ctx =
        ContextAttributes.add(Context.root(), Attributes.of(stringKey("ctx_attr"), "ctx"));

    try (Scope ignored = ctx.makeCurrent()) {
      Span span = tracer.spanBuilder("span").setAttribute("explicit_attr", "explicit").startSpan();
      span.end();
    }

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    SpanData spanData = spans.get(0);
    assertThat(spanData).hasAttributes(Attributes.of(stringKey("explicit_attr"), "explicit"));
    assertThat(spanData.getTotalAttributeCount()).isEqualTo(2);
  }
}
