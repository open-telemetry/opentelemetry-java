/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.BooleanTag;
import io.opentracing.tag.IntTag;
import io.opentracing.tag.StringTag;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TracerShimTest {

  static final io.opentelemetry.api.trace.Span INVALID_SPAN =
      io.opentelemetry.api.trace.Span.getInvalid();
  static final io.opentelemetry.api.baggage.Baggage EMPTY_BAGGAGE =
      io.opentelemetry.api.baggage.Baggage.empty();

  @RegisterExtension static OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  TracerShim tracerShim;
  TracerProvider provider;

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
    GlobalOpenTelemetry.set(otelTesting.getOpenTelemetry());

    provider = otelTesting.getOpenTelemetry().getTracerProvider();
    TextMapPropagator propagator =
        otelTesting.getOpenTelemetry().getPropagators().getTextMapPropagator();
    tracerShim = new TracerShim(provider, propagator, propagator);
  }

  @AfterEach
  void cleanup() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void defaultTracer() {
    assertThat(tracerShim.buildSpan("one")).isNotNull();
    assertThat(tracerShim.scopeManager()).isNotNull();
    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
  }

  @Test
  void activateSpan() {
    Span otSpan = tracerShim.buildSpan("one").start();
    io.opentelemetry.api.trace.Span actualSpan = ((SpanShim) otSpan).getSpan();

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);

    try (Scope scope = tracerShim.activateSpan(otSpan)) {
      assertThat(tracerShim.activeSpan()).isNotNull();
      assertThat(tracerShim.scopeManager().activeSpan()).isNotNull();
      assertThat(((SpanShim) tracerShim.activeSpan()).getSpan()).isSameAs(actualSpan);
      assertThat(((SpanShim) tracerShim.scopeManager().activeSpan()).getSpan())
          .isSameAs(actualSpan);

      assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(actualSpan);
      assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);
    }

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);
  }

  @Test
  void activateNullSpan() {
    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);

    Span otSpan = tracerShim.buildSpan("one").start();
    io.opentelemetry.api.trace.Span actualSpan = ((SpanShim) otSpan).getSpan();
    try (Scope scope1 = tracerShim.activateSpan(otSpan)) {
      try (Scope scope2 = tracerShim.activateSpan(null)) {
        assertThat(tracerShim.activeSpan()).isNull();
        assertThat(tracerShim.scopeManager().activeSpan()).isNull();
        assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
      }
      assertThat(tracerShim.activeSpan()).isSameAs(otSpan);
      assertThat(((SpanShim) tracerShim.activeSpan()).getSpan()).isSameAs(actualSpan);
    }

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);
  }

  @Test
  void activateSpan_withBaggage() {
    Span otSpan = tracerShim.buildSpan("one").start();
    otSpan.setBaggageItem("foo", "bar");
    otSpan.setBaggageItem("hello", "world");

    io.opentelemetry.api.trace.Span actualSpan = ((SpanShim) otSpan).getSpan();
    io.opentelemetry.api.baggage.Baggage actualBaggage =
        ((SpanContextShim) otSpan.context()).getBaggage();
    assertThat(
            io.opentelemetry.api.baggage.Baggage.builder()
                .put("foo", "bar")
                .put("hello", "world")
                .build())
        .isEqualTo(actualBaggage);

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);

    try (Scope scope = tracerShim.activateSpan(otSpan)) {
      assertThat(tracerShim.activeSpan()).isNotNull();
      assertThat(tracerShim.scopeManager().activeSpan()).isNotNull();
      assertThat(((SpanShim) tracerShim.activeSpan()).getSpan()).isSameAs(actualSpan);
      assertThat(((SpanShim) tracerShim.scopeManager().activeSpan()).getSpan())
          .isSameAs(actualSpan);

      assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(actualSpan);
      assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(actualBaggage);

      Span child = tracerShim.buildSpan("child").start();
      assertThat(child.getBaggageItem("foo")).isEqualTo("bar");
    }

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);
  }

  /* A Span should activate its related Baggage, which may mean implicitly
   * overriding any previous current Baggage value */
  @Test
  public void activateSpan_withExistingBaggage() {
    /* Span has empty Baggage as there's no parent nor previously set Baggage */
    Span otSpan = tracerShim.buildSpan("one").start();
    io.opentelemetry.api.trace.Span actualSpan = ((SpanShim) otSpan).getSpan();

    io.opentelemetry.api.baggage.Baggage otherBaggage =
        io.opentelemetry.api.baggage.Baggage.builder()
            .put("foo", "bar")
            .put("hello", "world")
            .build();

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);

    try (io.opentelemetry.context.Scope scope1 = otherBaggage.makeCurrent()) {
      try (Scope scope2 = tracerShim.activateSpan(otSpan)) {
        assertThat(tracerShim.activeSpan()).isNotNull();
        assertThat(tracerShim.scopeManager().activeSpan()).isNotNull();
        assertThat(((SpanShim) tracerShim.activeSpan()).getSpan()).isSameAs(actualSpan);
        assertThat(((SpanShim) tracerShim.scopeManager().activeSpan()).getSpan())
            .isSameAs(actualSpan);

        assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(actualSpan);
        assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);
      }
    }

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);
  }

  /*
   * Make sure that, upon activation from the Trace Shim layer, the
   * returned active Span shim is cached/stored in the context, so
   * we always get the *same* instance.
   */
  @Test
  void activateSpan_cacheSpanShim() {
    Span otSpan = tracerShim.buildSpan("one").start();
    io.opentelemetry.api.trace.Span actualSpan = ((SpanShim) otSpan).getSpan();

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);

    try (Scope scope = tracerShim.activateSpan(otSpan)) {
      assertThat(tracerShim.activeSpan()).isSameAs(tracerShim.activeSpan());
      assertThat(tracerShim.activeSpan()).isSameAs(tracerShim.scopeManager().activeSpan());

      assertThat(((SpanShim) tracerShim.activeSpan()).getSpan()).isSameAs(actualSpan);
    }

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
    assertThat(io.opentelemetry.api.trace.Span.current()).isSameAs(INVALID_SPAN);
    assertThat(io.opentelemetry.api.baggage.Baggage.current()).isSameAs(EMPTY_BAGGAGE);
  }

  /*
   * Create and activate a Span without the Shim layer, in order to verify
   * we keep on working as expected (even if not as efficiently).
   */
  @Test
  void activateSpan_withoutShim() {
    io.opentelemetry.api.trace.Span span =
        otelTesting.getOpenTelemetry().getTracer("opentracingshim").spanBuilder("one").startSpan();
    try (io.opentelemetry.context.Scope scope = span.makeCurrent()) {
      assertThat(tracerShim.activeSpan()).isNotNull();
      assertThat(tracerShim.scopeManager().activeSpan()).isNotNull();
      assertThat(((SpanShim) tracerShim.activeSpan()).getSpan()).isSameAs(span);
      assertThat(((SpanShim) tracerShim.scopeManager().activeSpan()).getSpan()).isSameAs(span);
    }

    assertThat(tracerShim.activeSpan()).isNull();
    assertThat(tracerShim.scopeManager().activeSpan()).isNull();
  }

  @Test
  void activeSpan_onlyBaggage() {
    io.opentelemetry.api.baggage.Baggage baggage =
        io.opentelemetry.api.baggage.Baggage.builder().put("foo", "bar").build();

    try (io.opentelemetry.context.Scope scope = Context.root().with(baggage).makeCurrent()) {
      Span span = tracerShim.activeSpan();
      assertThat(span).isNotNull();

      SpanShim spanShim = (SpanShim) span;
      assertThat(spanShim.getSpan()).isSameAs(INVALID_SPAN);
      assertThat(spanShim.getBaggage()).isSameAs(baggage);
    }
  }

  @Test
  void extract_nullContext() {
    SpanContext result =
        tracerShim.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(Collections.emptyMap()));
    assertThat(result).isNull();
  }

  @Test
  void inject_nullContext() {
    Map<String, String> map = new HashMap<>();
    tracerShim.inject(null, Format.Builtin.TEXT_MAP, new TextMapAdapter(map));
    assertThat(map).isEmpty();
  }

  @Test
  void inject_invalid() {
    Map<String, String> map = new HashMap<>();
    tracerShim.inject(mock(SpanContext.class), Format.Builtin.TEXT_MAP, new TextMapAdapter(map));
    assertThat(map).isEmpty();
  }

  @Test
  void inject_textMap() {
    Map<String, String> map = new HashMap<>();
    CustomTextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    CustomTextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();
    tracerShim = new TracerShim(provider, textMapPropagator, httpHeadersPropagator);
    io.opentelemetry.api.trace.Span span = tracerShim.tracer().spanBuilder("span").startSpan();
    SpanContext context = new SpanShim(span).context();

    tracerShim.inject(context, Format.Builtin.TEXT_MAP, new TextMapAdapter(map));
    assertThat(textMapPropagator.isInjected()).isTrue();
    assertThat(httpHeadersPropagator.isInjected()).isFalse();
  }

  @Test
  void inject_httpHeaders() {
    Map<String, String> map = new HashMap<>();
    CustomTextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    CustomTextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();
    tracerShim = new TracerShim(provider, textMapPropagator, httpHeadersPropagator);
    io.opentelemetry.api.trace.Span span = tracerShim.tracer().spanBuilder("span").startSpan();
    SpanContext context = new SpanShim(span).context();

    tracerShim.inject(context, Format.Builtin.HTTP_HEADERS, new TextMapAdapter(map));
    assertThat(textMapPropagator.isInjected()).isFalse();
    assertThat(httpHeadersPropagator.isInjected()).isTrue();
  }

  @Test
  void extract_textMap() {
    Map<String, String> map = new HashMap<>();
    CustomTextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    CustomTextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();
    tracerShim = new TracerShim(provider, textMapPropagator, httpHeadersPropagator);

    tracerShim.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(map));
    assertThat(textMapPropagator.isExtracted()).isTrue();
    assertThat(httpHeadersPropagator.isExtracted()).isFalse();
  }

  @Test
  void extract_httpHeaders() {
    Map<String, String> map = new HashMap<>();
    CustomTextMapPropagator textMapPropagator = new CustomTextMapPropagator();
    CustomTextMapPropagator httpHeadersPropagator = new CustomTextMapPropagator();
    tracerShim = new TracerShim(provider, textMapPropagator, httpHeadersPropagator);

    tracerShim.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(map));
    assertThat(textMapPropagator.isExtracted()).isFalse();
    assertThat(httpHeadersPropagator.isExtracted()).isTrue();
  }

  @Test
  void extract_onlyBaggage() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    tracerShim = new TracerShim(provider, propagator, TextMapPropagator.noop());

    io.opentelemetry.api.baggage.Baggage baggage =
        io.opentelemetry.api.baggage.Baggage.builder().put("foo", "bar").build();
    Map<String, String> map = new HashMap<>();
    propagator.inject(Context.root().with(baggage), map, Map::put);

    SpanContext spanContext = tracerShim.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(map));
    SpanContextShim spanContextShim = (SpanContextShim) spanContext;
    assertThat(spanContextShim.getSpanContext().isValid()).isFalse();
    assertThat(spanContextShim.getBaggage()).isEqualTo(baggage);
  }

  @Test
  void close_OpenTelemetrySdk() {
    SdkTracerProvider sdkProvider = mock(SdkTracerProvider.class);
    doThrow(new RuntimeException("testing error")).when(sdkProvider).close();

    Tracer tracerShim =
        OpenTracingShim.createTracerShim(
            OpenTelemetrySdk.builder().setTracerProvider(sdkProvider).build());
    tracerShim.close();

    verify(sdkProvider).close();

    Span otSpan = tracerShim.buildSpan(null).start();
    io.opentelemetry.api.trace.Span span = ((SpanShim) otSpan).getSpan();
    assertThat(span.getSpanContext().isValid()).isFalse();
  }

  @Test
  void close_GlobalOpenTelemetry() {
    GlobalOpenTelemetry.resetForTest();
    SdkTracerProvider sdkProvider = mock(SdkTracerProvider.class);
    doThrow(new RuntimeException("testing error")).when(sdkProvider).close();
    GlobalOpenTelemetry.set(OpenTelemetrySdk.builder().setTracerProvider(sdkProvider).build());

    Tracer tracerShim = OpenTracingShim.createTracerShim(GlobalOpenTelemetry.get());
    tracerShim.close();

    verify(sdkProvider).close();

    Span otSpan = tracerShim.buildSpan(null).start();
    io.opentelemetry.api.trace.Span span = ((SpanShim) otSpan).getSpan();
    assertThat(span.getSpanContext().isValid()).isFalse();
  }

  @Test
  void close_NoopOpenTelemetry() {
    Tracer tracerShim = OpenTracingShim.createTracerShim(OpenTelemetry.noop());
    tracerShim.close();

    Span otSpan = tracerShim.buildSpan(null).start();
    io.opentelemetry.api.trace.Span span = ((SpanShim) otSpan).getSpan();
    assertThat(span.getSpanContext().isValid()).isFalse();
  }

  @Test
  void doesNotCrash() {
    Span span =
        tracerShim
            .buildSpan("test")
            .asChildOf((Span) null)
            .asChildOf((SpanContext) null)
            .addReference(null, null)
            .addReference("parent", tracerShim.buildSpan("parent").start().context())
            .ignoreActiveSpan()
            .withTag((Tag<?>) null, null)
            .withTag("foo", (String) null)
            .withTag("bar", false)
            .withTag("cat", (Number) null)
            .withTag("dog", 0.0f)
            .withTag("bear", 10)
            .withTag(new StringTag("string"), "string")
            .withTag(new BooleanTag("boolean"), false)
            .withTag(new IntTag("int"), 10)
            .start();

    span.setTag((Tag<?>) null, null)
        .setTag("foo", (String) null)
        .setTag("bar", false)
        .setTag("cat", (Number) null)
        .setTag("dog", 0.0f)
        .setTag("bear", 10)
        .setTag(new StringTag("string"), "string")
        .setTag(new BooleanTag("boolean"), false)
        .setTag(new IntTag("int"), 10)
        .log(10, new HashMap<>())
        .log(20, "foo")
        .setBaggageItem(null, null)
        .setOperationName("name")
        .setTag(Tags.ERROR.getKey(), "true");

    assertThat(((SpanShim) span).getSpan().isRecording()).isTrue();
  }

  @Test
  void noopDoesNotCrash() {
    Tracer tracerShim = OpenTracingShim.createTracerShim(OpenTelemetry.noop());

    Span span =
        tracerShim
            .buildSpan("test")
            .asChildOf((Span) null)
            .asChildOf((SpanContext) null)
            .addReference(null, null)
            .ignoreActiveSpan()
            .withTag((Tag<?>) null, null)
            .withTag("foo", (String) null)
            .withTag("bar", false)
            .withTag("cat", (Number) null)
            .withStartTimestamp(0)
            .start();

    assertThat(((SpanShim) span).getSpan().isRecording()).isFalse();
  }
}
