/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkSpanBuilderTest {

  private static final String SPAN_NAME = "span_name";
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          TraceId.fromLongs(1000, 1000),
          SpanId.fromLong(3000),
          TraceFlags.getSampled(),
          TraceState.getDefault());

  @Mock private SpanProcessor mockedSpanProcessor;

  private SdkTracer sdkTracer;

  @BeforeEach
  public void setUp() {
    SdkTracerProvider tracerSdkFactory =
        SdkTracerProvider.builder().addSpanProcessor(mockedSpanProcessor).build();
    sdkTracer = (SdkTracer) tracerSdkFactory.get("SpanBuilderSdkTest");

    Mockito.when(mockedSpanProcessor.isStartRequired()).thenReturn(true);
    Mockito.when(mockedSpanProcessor.isEndRequired()).thenReturn(true);
  }

  @Test
  void setSpanKind_null() {
    assertThatThrownBy(() -> sdkTracer.spanBuilder(SPAN_NAME).setSpanKind(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void setParent_null() {
    assertThatThrownBy(() -> sdkTracer.spanBuilder(SPAN_NAME).setParent(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addLink() {
    // Verify methods do not crash.
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.addLink(Span.getInvalid().getSpanContext());
    spanBuilder.addLink(Span.getInvalid().getSpanContext(), Attributes.empty());

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getLinks()).hasSize(2);
    } finally {
      span.end();
    }
  }

  @Test
  void truncateLink() {
    final int maxNumberOfLinks = 8;
    TraceConfig traceConfig = TraceConfig.builder().setMaxNumberOfLinks(maxNumberOfLinks).build();
    TracerProvider tracerProvider = SdkTracerProvider.builder().setTraceConfig(traceConfig).build();
    // Verify methods do not crash.
    SpanBuilder spanBuilder = tracerProvider.get("test").spanBuilder(SPAN_NAME);
    for (int i = 0; i < 2 * maxNumberOfLinks; i++) {
      spanBuilder.addLink(sampledSpanContext);
    }
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      SpanData spanData = span.toSpanData();
      List<LinkData> links = spanData.getLinks();
      assertThat(links).hasSize(maxNumberOfLinks);
      for (int i = 0; i < maxNumberOfLinks; i++) {
        assertThat(links.get(i)).isEqualTo(LinkData.create(sampledSpanContext));
        assertThat(spanData.getTotalRecordedLinks()).isEqualTo(2 * maxNumberOfLinks);
      }
    } finally {
      span.end();
    }
  }

  @Test
  void truncateLinkAttributes() {
    TraceConfig traceConfig = TraceConfig.builder().setMaxNumberOfAttributesPerLink(1).build();
    TracerProvider tracerProvider = SdkTracerProvider.builder().setTraceConfig(traceConfig).build();
    // Verify methods do not crash.
    SpanBuilder spanBuilder = tracerProvider.get("test").spanBuilder(SPAN_NAME);
    Attributes attributes =
        Attributes.of(
            stringKey("key0"), "str",
            stringKey("key1"), "str",
            stringKey("key2"), "str");
    spanBuilder.addLink(sampledSpanContext, attributes);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getLinks())
          .containsExactly(
              LinkData.create(sampledSpanContext, Attributes.of(stringKey("key0"), "str"), 3));
    } finally {
      span.end();
    }
  }

  @Test
  void addLink_NoEffectAfterStartSpan() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.addLink(sampledSpanContext);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getLinks())
          .containsExactly(LinkData.create(sampledSpanContext, Attributes.empty()));
      // Use a different sampledSpanContext to ensure no logic that avoids duplicate links makes
      // this test to pass.
      spanBuilder.addLink(
          SpanContext.create(
              TraceId.fromLongs(2000, 2000),
              SpanId.fromLong(4000),
              TraceFlags.getSampled(),
              TraceState.getDefault()));
      assertThat(span.toSpanData().getLinks())
          .containsExactly(LinkData.create(sampledSpanContext, Attributes.empty()));
    } finally {
      span.end();
    }
  }

  @Test
  void addLinkSpanContext_null() {
    assertThatThrownBy(() -> sdkTracer.spanBuilder(SPAN_NAME).addLink(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addLinkSpanContextAttributes_nullContext() {
    assertThatThrownBy(() -> sdkTracer.spanBuilder(SPAN_NAME).addLink(null, Attributes.empty()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addLinkSpanContextAttributes_nullAttributes() {
    assertThatThrownBy(
            () ->
                sdkTracer.spanBuilder(SPAN_NAME).addLink(Span.getInvalid().getSpanContext(), null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void setAttribute() {
    SpanBuilder spanBuilder =
        sdkTracer
            .spanBuilder(SPAN_NAME)
            .setAttribute("string", "value")
            .setAttribute("long", 12345L)
            .setAttribute("double", .12345)
            .setAttribute("boolean", true)
            .setAttribute(stringKey("stringAttribute"), "attrvalue");

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      SpanData spanData = span.toSpanData();
      Attributes attrs = spanData.getAttributes();
      assertThat(attrs.size()).isEqualTo(5);
      assertThat(attrs.get(stringKey("string"))).isEqualTo("value");
      assertThat(attrs.get(longKey("long"))).isEqualTo(12345L);
      assertThat(attrs.get(doubleKey("double"))).isEqualTo(0.12345);
      assertThat(attrs.get(booleanKey("boolean"))).isEqualTo(true);
      assertThat(attrs.get(stringKey("stringAttribute"))).isEqualTo("attrvalue");
      assertThat(spanData.getTotalAttributeCount()).isEqualTo(5);
    } finally {
      span.end();
    }
  }

  @Test
  void setAttribute_afterEnd() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("string", "value");
    spanBuilder.setAttribute("long", 12345L);
    spanBuilder.setAttribute("double", .12345);
    spanBuilder.setAttribute("boolean", true);
    spanBuilder.setAttribute(stringKey("stringAttribute"), "attrvalue");

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      Attributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.size()).isEqualTo(5);
      assertThat(attrs.get(stringKey("string"))).isEqualTo("value");
      assertThat(attrs.get(longKey("long"))).isEqualTo(12345L);
      assertThat(attrs.get(doubleKey("double"))).isEqualTo(0.12345);
      assertThat(attrs.get(booleanKey("boolean"))).isEqualTo(true);
      assertThat(attrs.get(stringKey("stringAttribute"))).isEqualTo("attrvalue");
    } finally {
      span.end();
    }

    span.setAttribute("string2", "value");
    span.setAttribute("long2", 12345L);
    span.setAttribute("double2", .12345);
    span.setAttribute("boolean2", true);
    span.setAttribute(stringKey("stringAttribute2"), "attrvalue");

    Attributes attrs = span.toSpanData().getAttributes();
    assertThat(attrs.size()).isEqualTo(5);
    assertThat(attrs.get(stringKey("string2"))).isNull();
    assertThat(attrs.get(longKey("long2"))).isNull();
    assertThat(attrs.get(doubleKey("double2"))).isNull();
    assertThat(attrs.get(booleanKey("boolean2"))).isNull();
    assertThat(attrs.get(stringKey("stringAttribute2"))).isNull();
  }

  @Test
  void setAttribute_emptyArrayAttributeValue() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute(stringArrayKey("stringArrayAttribute"), emptyList());
    spanBuilder.setAttribute(booleanArrayKey("boolArrayAttribute"), emptyList());
    spanBuilder.setAttribute(longArrayKey("longArrayAttribute"), emptyList());
    spanBuilder.setAttribute(doubleArrayKey("doubleArrayAttribute"), emptyList());
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(4);
  }

  @Test
  void setAttribute_nullStringValue() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute("nullString", null);
    spanBuilder.setAttribute(stringKey("nullStringAttributeValue"), null);
    spanBuilder.setAttribute(stringKey("emptyStringAttributeValue"), "");
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
  }

  @Test
  void setAttribute_onlyNullStringValue() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute(stringKey("nullStringAttributeValue"), null);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().isEmpty()).isTrue();
  }

  @Test
  void setAttribute_NoEffectAfterStartSpan() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("key1", "value1");
    spanBuilder.setAttribute("key2", "value2");
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();

    Attributes beforeAttributes = span.toSpanData().getAttributes();
    assertThat(beforeAttributes.size()).isEqualTo(2);
    assertThat(beforeAttributes.get(stringKey("key1"))).isEqualTo("value1");
    assertThat(beforeAttributes.get(stringKey("key2"))).isEqualTo("value2");

    spanBuilder.setAttribute("key3", "value3");

    Attributes afterAttributes = span.toSpanData().getAttributes();
    assertThat(afterAttributes.size()).isEqualTo(2);
    assertThat(afterAttributes.get(stringKey("key1"))).isEqualTo("value1");
    assertThat(afterAttributes.get(stringKey("key2"))).isEqualTo("value2");
  }

  @Test
  void setAttribute_nullAttributeValue() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute("nullString", null);
    spanBuilder.setAttribute(stringKey("nullStringAttributeValue"), null);
    spanBuilder.setAttribute(stringKey("emptyStringAttributeValue"), "");
    spanBuilder.setAttribute("longAttribute", 0L);
    spanBuilder.setAttribute("boolAttribute", false);
    spanBuilder.setAttribute("doubleAttribute", 0.12345f);
    spanBuilder.setAttribute(stringArrayKey("stringArrayAttribute"), Arrays.asList("", null));
    spanBuilder.setAttribute(booleanArrayKey("boolArrayAttribute"), Arrays.asList(true, null));
    spanBuilder.setAttribute(longArrayKey("longArrayAttribute"), Arrays.asList(12345L, null));
    spanBuilder.setAttribute(doubleArrayKey("doubleArrayAttribute"), Arrays.asList(1.2345, null));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
  }

  @Test
  void setAttribute_nullAttributeValue_afterEnd() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute(stringKey("emptyStringAttributeValue"), "");
    spanBuilder.setAttribute("longAttribute", 0L);
    spanBuilder.setAttribute("boolAttribute", false);
    spanBuilder.setAttribute("doubleAttribute", 0.12345f);
    spanBuilder.setAttribute(stringArrayKey("stringArrayAttribute"), Arrays.asList("", null));
    spanBuilder.setAttribute(booleanArrayKey("boolArrayAttribute"), Arrays.asList(true, null));
    spanBuilder.setAttribute(longArrayKey("longArrayAttribute"), Arrays.asList(12345L, null));
    spanBuilder.setAttribute(doubleArrayKey("doubleArrayAttribute"), Arrays.asList(1.2345, null));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
    span.end();
    span.setAttribute("emptyString", null);
    span.setAttribute(stringKey("emptyStringAttributeValue"), null);
    span.setAttribute(longKey("longAttribute"), null);
    span.setAttribute(booleanKey("boolAttribute"), null);
    span.setAttribute(doubleKey("doubleAttribute"), null);
    span.setAttribute(stringArrayKey("stringArrayAttribute"), null);
    span.setAttribute(booleanArrayKey("boolArrayAttribute"), null);
    span.setAttribute(longArrayKey("longArrayAttribute"), null);
    span.setAttribute(doubleArrayKey("doubleArrayAttribute"), null);
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
  }

  @Test
  void droppingAttributes() {
    final int maxNumberOfAttrs = 8;
    TraceConfig traceConfig =
        TraceConfig.builder().setMaxNumberOfAttributes(maxNumberOfAttrs).build();
    TracerProvider tracerProvider = SdkTracerProvider.builder().setTraceConfig(traceConfig).build();
    // Verify methods do not crash.
    SpanBuilder spanBuilder = tracerProvider.get("test").spanBuilder(SPAN_NAME);
    for (int i = 0; i < 2 * maxNumberOfAttrs; i++) {
      spanBuilder.setAttribute("key" + i, i);
    }
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      Attributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.size()).isEqualTo(maxNumberOfAttrs);
      for (int i = 0; i < maxNumberOfAttrs; i++) {
        assertThat(attrs.get(longKey("key" + i))).isEqualTo(i);
      }
    } finally {
      span.end();
    }
  }

  @Test
  public void tooLargeAttributeValuesAreTruncated() {
    TraceConfig traceConfig = TraceConfig.builder().setMaxLengthOfAttributeValues(10).build();
    TracerProvider tracerProvider = SdkTracerProvider.builder().setTraceConfig(traceConfig).build();
    // Verify methods do not crash.
    SpanBuilder spanBuilder = tracerProvider.get("test").spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("builderStringNull", null);
    spanBuilder.setAttribute("builderStringSmall", "small");
    spanBuilder.setAttribute("builderStringLarge", "very large string that we have to cut");
    spanBuilder.setAttribute("builderLong", 42L);
    spanBuilder.setAttribute(
        stringKey("builderStringLargeValue"), "very large string that we have to cut");
    spanBuilder.setAttribute(
        stringArrayKey("builderStringArray"),
        Arrays.asList("small", null, "very large string that we have to cut"));

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    span.setAttribute("spanStringSmall", "small");
    span.setAttribute("spanStringLarge", "very large string that we have to cut");
    span.setAttribute("spanLong", 42L);
    span.setAttribute(stringKey("spanStringLarge"), "very large string that we have to cut");
    span.setAttribute(
        stringArrayKey("spanStringArray"),
        Arrays.asList("small", null, "very large string that we have to cut"));

    try {
      Attributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.get(stringKey("builderStringNull"))).isEqualTo(null);
      assertThat(attrs.get(stringKey("builderStringSmall"))).isEqualTo("small");
      assertThat(attrs.get(stringKey("builderStringLarge"))).isEqualTo("very large");
      assertThat(attrs.get(longKey("builderLong"))).isEqualTo(42L);
      assertThat(attrs.get(stringKey("builderStringLargeValue"))).isEqualTo("very large");
      assertThat(attrs.get(stringArrayKey("builderStringArray")))
          .isEqualTo(Arrays.asList("small", null, "very large"));

      assertThat(attrs.get(stringKey("spanStringSmall"))).isEqualTo("small");
      assertThat(attrs.get(stringKey("spanStringLarge"))).isEqualTo("very large");
      assertThat(attrs.get(longKey("spanLong"))).isEqualTo(42L);
      assertThat(attrs.get(stringKey("spanStringLarge"))).isEqualTo("very large");
      assertThat(attrs.get(stringArrayKey("spanStringArray")))
          .isEqualTo(Arrays.asList("small", null, "very large"));
    } finally {
      span.end();
    }
  }

  @Test
  void addAttributes_OnlyViaSampler() {

    Sampler sampler =
        new Sampler() {
          @Override
          public SamplingResult shouldSample(
              Context parentContext,
              String traceId,
              String name,
              SpanKind spanKind,
              Attributes attributes,
              List<LinkData> parentLinks) {
            return SamplingResult.create(
                SamplingDecision.RECORD_AND_SAMPLE,
                Attributes.builder().put("cat", "meow").build());
          }

          @Override
          public String getDescription() {
            return "test";
          }
        };
    TracerProvider tracerProvider = SdkTracerProvider.builder().setSampler(sampler).build();
    // Verify methods do not crash.
    SpanBuilder spanBuilder = tracerProvider.get("test").spanBuilder(SPAN_NAME);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    span.end();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(1);
    assertThat(span.toSpanData().getAttributes().get(stringKey("cat"))).isEqualTo("meow");
  }

  @Test
  void recordEvents_default() {
    Span span = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.isRecording()).isTrue();
    } finally {
      span.end();
    }
  }

  @Test
  void kind_default() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan) sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(SpanKind.INTERNAL);
    } finally {
      span.end();
    }
  }

  @Test
  void kind() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            sdkTracer.spanBuilder(SPAN_NAME).setSpanKind(SpanKind.CONSUMER).startSpan();
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(SpanKind.CONSUMER);
    } finally {
      span.end();
    }
  }

  @Test
  void sampler() {
    Span span =
        SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOff())
            .build()
            .get("test")
            .spanBuilder(SPAN_NAME)
            .startSpan();
    try {
      assertThat(span.getSpanContext().isSampled()).isFalse();
    } finally {
      span.end();
    }
  }

  @Test
  void sampler_decisionAttributes() {
    final String samplerAttributeName = "sampler-attribute";
    AttributeKey<String> samplerAttributeKey = stringKey(samplerAttributeName);
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            SdkTracerProvider.builder()
                .setSampler(
                    new Sampler() {
                      @Override
                      public SamplingResult shouldSample(
                          @Nullable Context parentContext,
                          String traceId,
                          String name,
                          SpanKind spanKind,
                          Attributes attributes,
                          List<LinkData> parentLinks) {
                        return new SamplingResult() {
                          @Override
                          public SamplingDecision getDecision() {
                            return SamplingDecision.RECORD_AND_SAMPLE;
                          }

                          @Override
                          public Attributes getAttributes() {
                            return Attributes.of(samplerAttributeKey, "bar");
                          }
                        };
                      }

                      @Override
                      public String getDescription() {
                        return "test sampler";
                      }
                    })
                .addSpanProcessor(mockedSpanProcessor)
                .build()
                .get("test")
                .spanBuilder(SPAN_NAME)
                .setAttribute(samplerAttributeKey, "none")
                .startSpan();
    try {
      assertThat(span.getSpanContext().isSampled()).isTrue();
      assertThat(span.toSpanData().getAttributes().get(samplerAttributeKey)).isNotNull();
      assertThat(span.toSpanData().getTraceState()).isEqualTo(TraceState.getDefault());
    } finally {
      span.end();
    }
  }

  @Test
  void sampler_updatedTraceState() {
    final String samplerAttributeName = "sampler-attribute";
    AttributeKey<String> samplerAttributeKey = stringKey(samplerAttributeName);
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            SdkTracerProvider.builder()
                .setSampler(
                    new Sampler() {
                      @Override
                      public SamplingResult shouldSample(
                          Context parentContext,
                          String traceId,
                          String name,
                          SpanKind spanKind,
                          Attributes attributes,
                          List<LinkData> parentLinks) {
                        return new SamplingResult() {
                          @Override
                          public SamplingDecision getDecision() {
                            return SamplingDecision.RECORD_AND_SAMPLE;
                          }

                          @Override
                          public Attributes getAttributes() {
                            return Attributes.empty();
                          }

                          @Override
                          public TraceState getUpdatedTraceState(TraceState parentTraceState) {
                            return parentTraceState.toBuilder().set("newkey", "newValue").build();
                          }
                        };
                      }

                      @Override
                      public String getDescription() {
                        return "test sampler";
                      }
                    })
                .build()
                .get("test")
                .spanBuilder(SPAN_NAME)
                .setAttribute(samplerAttributeKey, "none")
                .startSpan();
    try {
      assertThat(span.getSpanContext().isSampled()).isTrue();
      assertThat(span.toSpanData().getAttributes().get(samplerAttributeKey)).isNotNull();
      assertThat(span.toSpanData().getTraceState())
          .isEqualTo(TraceState.builder().set("newkey", "newValue").build());
    } finally {
      span.end();
    }
  }

  // TODO(anuraaga): Is this test correct? It's not sampled
  @Test
  void sampledViaParentLinks() {
    Span span =
        SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOff())
            .build()
            .get("test")
            .spanBuilder(SPAN_NAME)
            .startSpan();
    try {
      assertThat(span.getSpanContext().isSampled()).isFalse();
    } finally {
      if (span != null) {
        span.end();
      }
    }
  }

  @Test
  void noParent() {
    Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      Span span = sdkTracer.spanBuilder(SPAN_NAME).setNoParent().startSpan();
      try {
        assertThat(span.getSpanContext().getTraceId())
            .isNotEqualTo(parent.getSpanContext().getTraceId());
        Mockito.verify(mockedSpanProcessor)
            .onStart(Mockito.same(Context.root()), Mockito.same((ReadWriteSpan) span));
        Span spanNoParent =
            sdkTracer
                .spanBuilder(SPAN_NAME)
                .setNoParent()
                .setParent(Context.current())
                .setNoParent()
                .startSpan();
        try {
          assertThat(span.getSpanContext().getTraceId())
              .isNotEqualTo(parent.getSpanContext().getTraceId());
          Mockito.verify(mockedSpanProcessor)
              .onStart(Mockito.same(Context.root()), Mockito.same((ReadWriteSpan) spanNoParent));
        } finally {
          spanNoParent.end();
        }
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void noParent_override() {
    final Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      final Context parentContext = Context.current().with(parent);
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              sdkTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(parentContext).startSpan();
      try {
        Mockito.verify(mockedSpanProcessor)
            .onStart(Mockito.same(parentContext), Mockito.same((ReadWriteSpan) span));
        assertThat(span.getSpanContext().getTraceId())
            .isEqualTo(parent.getSpanContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getSpanContext().getSpanId());

        final Context parentContext2 = Context.current().with(parent);
        RecordEventsReadableSpan span2 =
            (RecordEventsReadableSpan)
                sdkTracer
                    .spanBuilder(SPAN_NAME)
                    .setNoParent()
                    .setParent(parentContext2)
                    .startSpan();
        try {
          Mockito.verify(mockedSpanProcessor)
              .onStart(Mockito.same(parentContext2), Mockito.same((ReadWriteSpan) span2));
          assertThat(span2.getSpanContext().getTraceId())
              .isEqualTo(parent.getSpanContext().getTraceId());
        } finally {
          span2.end();
        }
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void overrideNoParent_remoteParent() {
    Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try {

      final Context parentContext = Context.current().with(parent);
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              sdkTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(parentContext).startSpan();
      try {
        Mockito.verify(mockedSpanProcessor)
            .onStart(Mockito.same(parentContext), Mockito.same((ReadWriteSpan) span));
        assertThat(span.getSpanContext().getTraceId())
            .isEqualTo(parent.getSpanContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getSpanContext().getSpanId());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void parent_fromContext() {
    final Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    final Context context = Context.current().with(parent);
    try {
      final RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              sdkTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(context).startSpan();
      try {
        Mockito.verify(mockedSpanProcessor)
            .onStart(Mockito.same(context), Mockito.same((ReadWriteSpan) span));
        assertThat(span.getSpanContext().getTraceId())
            .isEqualTo(parent.getSpanContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getSpanContext().getSpanId());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void parent_fromEmptyContext() {
    Context emptyContext = Context.current();
    Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      RecordEventsReadableSpan span;
      try (Scope scope = parent.makeCurrent()) {
        span =
            (RecordEventsReadableSpan)
                sdkTracer.spanBuilder(SPAN_NAME).setParent(emptyContext).startSpan();
      }

      try {
        Mockito.verify(mockedSpanProcessor)
            .onStart(Mockito.same(emptyContext), Mockito.same((ReadWriteSpan) span));
        assertThat(span.getSpanContext().getTraceId())
            .isNotEqualTo(parent.getSpanContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId())
            .isNotEqualTo(parent.getSpanContext().getSpanId());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void parentCurrentSpan() {
    Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      final Context implicitParent = Context.current();
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) sdkTracer.spanBuilder(SPAN_NAME).startSpan();
      try {
        Mockito.verify(mockedSpanProcessor)
            .onStart(Mockito.same(implicitParent), Mockito.same((ReadWriteSpan) span));
        assertThat(span.getSpanContext().getTraceId())
            .isEqualTo(parent.getSpanContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getSpanContext().getSpanId());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void parent_invalidContext() {
    Span parent = Span.getInvalid();

    final Context parentContext = Context.current().with(parent);
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            sdkTracer.spanBuilder(SPAN_NAME).setParent(parentContext).startSpan();
    try {
      Mockito.verify(mockedSpanProcessor)
          .onStart(
              ArgumentMatchers.same(parentContext), ArgumentMatchers.same((ReadWriteSpan) span));
      assertThat(span.getSpanContext().getTraceId())
          .isNotEqualTo(parent.getSpanContext().getTraceId());
      assertThat(SpanId.isValid(span.toSpanData().getParentSpanId())).isFalse();
    } finally {
      span.end();
    }
  }

  @Test
  void startTimestamp_numeric() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            sdkTracer
                .spanBuilder(SPAN_NAME)
                .setStartTimestamp(10, TimeUnit.NANOSECONDS)
                .startSpan();
    span.end();
    assertThat(span.toSpanData().getStartEpochNanos()).isEqualTo(10);
  }

  @Test
  void startTimestamp_instant() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            sdkTracer
                .spanBuilder(SPAN_NAME)
                .setStartTimestamp(Instant.ofEpochMilli(100))
                .startSpan();
    span.end();
    assertThat(span.toSpanData().getStartEpochNanos())
        .isEqualTo(TimeUnit.MILLISECONDS.toNanos(100));
  }

  @Test
  void startTimestamp_null() {
    assertThatThrownBy(
            () -> sdkTracer.spanBuilder(SPAN_NAME).setStartTimestamp(-1, TimeUnit.NANOSECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Negative startTimestamp");
  }

  @Test
  void parent_clockIsSame() {
    Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try (Scope scope = parent.makeCurrent()) {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) sdkTracer.spanBuilder(SPAN_NAME).startSpan();

      assertThat(span.getClock()).isSameAs(((RecordEventsReadableSpan) parent).getClock());
    } finally {
      parent.end();
    }
  }

  @Test
  void parentCurrentSpan_clockIsSame() {
    Span parent = sdkTracer.spanBuilder(SPAN_NAME).startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) sdkTracer.spanBuilder(SPAN_NAME).startSpan();

      assertThat(span.getClock()).isSameAs(((RecordEventsReadableSpan) parent).getClock());
    } finally {
      parent.end();
    }
  }

  @Test
  void isSampled() {
    assertThat(SdkSpanBuilder.isSampled(SamplingDecision.DROP)).isFalse();
    assertThat(SdkSpanBuilder.isSampled(SamplingDecision.RECORD_ONLY)).isFalse();
    assertThat(SdkSpanBuilder.isSampled(SamplingDecision.RECORD_AND_SAMPLE)).isTrue();
  }

  @Test
  void isRecording() {
    assertThat(SdkSpanBuilder.isRecording(SamplingDecision.DROP)).isFalse();
    assertThat(SdkSpanBuilder.isRecording(SamplingDecision.RECORD_ONLY)).isTrue();
    assertThat(SdkSpanBuilder.isRecording(SamplingDecision.RECORD_AND_SAMPLE)).isTrue();
  }

  // SpanData is very commonly used in unit tests, we want the toString to make sure it's relatively
  // easy to understand failure messages.
  // TODO(anuraaga): Currently it isn't - we even return the same (or maybe incorrect?) stuff twice.
  // Improve the toString.
  @Test
  void spanDataToString() {
    SpanBuilder spanBuilder = sdkTracer.spanBuilder(SPAN_NAME);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    span.setAttribute("http.status_code", 500);
    span.setAttribute("http.url", "https://opentelemetry.io");
    span.setStatus(StatusCode.ERROR, "error");
    span.end();

    assertThat(span.toSpanData().toString())
        .matches(
            "SpanData\\{spanContext=ImmutableSpanContext\\{"
                + "traceId=[0-9a-f]{32}, "
                + "spanId=[0-9a-f]{16}, "
                + "traceFlags=1, "
                + "traceState=ArrayBasedTraceState\\{entries=\\[]}, remote=false}, "
                + "parentSpanContext=ImmutableSpanContext\\{"
                + "traceId=00000000000000000000000000000000, "
                + "spanId=0000000000000000, "
                + "traceFlags=0, "
                + "traceState=ArrayBasedTraceState\\{entries=\\[]}, remote=false}, "
                + "resource=Resource\\{attributes=\\{service.name=\"unknown_service:java\", "
                + "telemetry.sdk.language=\"java\", telemetry.sdk.name=\"opentelemetry\", "
                + "telemetry.sdk.version=\"\\d+.\\d+.\\d+(-SNAPSHOT)?\"}}, "
                + "instrumentationLibraryInfo=InstrumentationLibraryInfo\\{"
                + "name=SpanBuilderSdkTest, version=null}, "
                + "name=span_name, "
                + "kind=INTERNAL, "
                + "startEpochNanos=[0-9]+, "
                + "endEpochNanos=[0-9]+, "
                + "attributes=AttributesMap\\{data=\\{http.status_code=500, "
                + "http.url=https://opentelemetry.io}, capacity=1000, totalAddedValues=2}, "
                + "totalAttributeCount=2, "
                + "events=\\[], "
                + "totalRecordedEvents=0, "
                + "links=\\[], "
                + "totalRecordedLinks=0, "
                + "status=ImmutableStatusData\\{statusCode=ERROR, description=error}, "
                + "hasEnded=true}");
  }
}
