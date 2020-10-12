/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.common.AttributeKey.booleanKey;
import static io.opentelemetry.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.common.AttributeKey.doubleKey;
import static io.opentelemetry.common.AttributeKey.longArrayKey;
import static io.opentelemetry.common.AttributeKey.longKey;
import static io.opentelemetry.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.common.AttributeKey.stringKey;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanBuilderSdk}. */
class SpanBuilderSdkTest {
  private static final String SPAN_NAME = "span_name";
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          TraceId.fromLongs(1000, 1000),
          SpanId.fromLong(3000),
          TraceFlags.getSampled(),
          TraceState.getDefault());

  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final TracerSdk tracerSdk = (TracerSdk) tracerSdkFactory.get("SpanBuilderSdkTest");

  @Test
  void setSpanKind_null() {
    assertThrows(
        NullPointerException.class, () -> tracerSdk.spanBuilder(SPAN_NAME).setSpanKind(null));
  }

  @Test
  void setParent_null() {
    assertThrows(
        NullPointerException.class, () -> tracerSdk.spanBuilder(SPAN_NAME).setParent(null));
  }

  @Test
  void addLink() {
    // Verify methods do not crash.
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext());
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext(), Attributes.empty());

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
    TraceConfig traceConfig =
        tracerSdkFactory
            .getActiveTraceConfig()
            .toBuilder()
            .setMaxNumberOfLinks(maxNumberOfLinks)
            .build();
    tracerSdkFactory.updateActiveTraceConfig(traceConfig);
    // Verify methods do not crash.
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    for (int i = 0; i < 2 * maxNumberOfLinks; i++) {
      spanBuilder.addLink(sampledSpanContext);
    }
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      SpanData spanData = span.toSpanData();
      List<SpanData.Link> links = spanData.getLinks();
      assertThat(links).hasSize(maxNumberOfLinks);
      for (int i = 0; i < maxNumberOfLinks; i++) {
        assertThat(links.get(i)).isEqualTo(Link.create(sampledSpanContext));
        assertThat(spanData.getTotalRecordedLinks()).isEqualTo(2 * maxNumberOfLinks);
      }
    } finally {
      span.end();
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  void truncateLinkAttributes() {
    TraceConfig traceConfig =
        tracerSdkFactory
            .getActiveTraceConfig()
            .toBuilder()
            .setMaxNumberOfAttributesPerLink(1)
            .build();
    tracerSdkFactory.updateActiveTraceConfig(traceConfig);
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
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
              Link.create(sampledSpanContext, Attributes.of(stringKey("key0"), "str"), 3));
    } finally {
      span.end();
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  void addLink_NoEffectAfterStartSpan() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.addLink(sampledSpanContext);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getLinks())
          .containsExactly(Link.create(sampledSpanContext, Attributes.empty()));
      // Use a different sampledSpanContext to ensure no logic that avoids duplicate links makes
      // this test to pass.
      spanBuilder.addLink(
          SpanContext.create(
              TraceId.fromLongs(2000, 2000),
              SpanId.fromLong(4000),
              TraceFlags.getSampled(),
              TraceState.getDefault()));
      assertThat(span.toSpanData().getLinks())
          .containsExactly(Link.create(sampledSpanContext, Attributes.empty()));
    } finally {
      span.end();
    }
  }

  @Test
  void addLinkSpanContext_null() {
    assertThrows(NullPointerException.class, () -> tracerSdk.spanBuilder(SPAN_NAME).addLink(null));
  }

  @Test
  void addLinkSpanContextAttributes_nullContext() {
    assertThrows(
        NullPointerException.class,
        () -> tracerSdk.spanBuilder(SPAN_NAME).addLink(null, Attributes.empty()));
  }

  @Test
  void addLinkSpanContextAttributes_nullAttributes() {
    assertThrows(
        NullPointerException.class,
        () ->
            tracerSdk.spanBuilder(SPAN_NAME).addLink(DefaultSpan.getInvalid().getContext(), null));
  }

  @Test
  void setAttribute() {
    Span.Builder spanBuilder =
        tracerSdk
            .spanBuilder(SPAN_NAME)
            .setAttribute("string", "value")
            .setAttribute("long", 12345L)
            .setAttribute("double", .12345)
            .setAttribute("boolean", true)
            .setAttribute(stringKey("stringAttribute"), "attrvalue");

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      SpanData spanData = span.toSpanData();
      ReadableAttributes attrs = spanData.getAttributes();
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
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("string", "value");
    spanBuilder.setAttribute("long", 12345L);
    spanBuilder.setAttribute("double", .12345);
    spanBuilder.setAttribute("boolean", true);
    spanBuilder.setAttribute(stringKey("stringAttribute"), "attrvalue");

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      ReadableAttributes attrs = span.toSpanData().getAttributes();
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

    ReadableAttributes attrs = span.toSpanData().getAttributes();
    assertThat(attrs.size()).isEqualTo(5);
    assertThat(attrs.get(stringKey("string2"))).isNull();
    assertThat(attrs.get(longKey("long2"))).isNull();
    assertThat(attrs.get(doubleKey("double2"))).isNull();
    assertThat(attrs.get(booleanKey("boolean2"))).isNull();
    assertThat(attrs.get(stringKey("stringAttribute2"))).isNull();
  }

  @Test
  void setAttribute_emptyArrayAttributeValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute(stringArrayKey("stringArrayAttribute"), emptyList());
    spanBuilder.setAttribute(booleanArrayKey("boolArrayAttribute"), emptyList());
    spanBuilder.setAttribute(longArrayKey("longArrayAttribute"), emptyList());
    spanBuilder.setAttribute(doubleArrayKey("doubleArrayAttribute"), emptyList());
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(4);
  }

  @Test
  void setAttribute_nullStringValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute("nullString", null);
    spanBuilder.setAttribute(stringKey("nullStringAttributeValue"), null);
    spanBuilder.setAttribute(stringKey("emptyStringAttributeValue"), "");
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
  }

  @Test
  void setAttribute_onlyNullStringValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute(stringKey("nullStringAttributeValue"), null);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().isEmpty()).isTrue();
  }

  @Test
  void setAttribute_NoEffectAfterStartSpan() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("key1", "value1");
    spanBuilder.setAttribute("key2", "value2");
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();

    ReadableAttributes beforeAttributes = span.toSpanData().getAttributes();
    assertThat(beforeAttributes.size()).isEqualTo(2);
    assertThat(beforeAttributes.get(stringKey("key1"))).isEqualTo("value1");
    assertThat(beforeAttributes.get(stringKey("key2"))).isEqualTo("value2");

    spanBuilder.setAttribute("key3", "value3");

    ReadableAttributes afterAttributes = span.toSpanData().getAttributes();
    assertThat(afterAttributes.size()).isEqualTo(2);
    assertThat(afterAttributes.get(stringKey("key1"))).isEqualTo("value1");
    assertThat(afterAttributes.get(stringKey("key2"))).isEqualTo("value2");
  }

  @Test
  void setAttribute_nullAttributeValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
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
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
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
        tracerSdkFactory
            .getActiveTraceConfig()
            .toBuilder()
            .setMaxNumberOfAttributes(maxNumberOfAttrs)
            .build();
    tracerSdkFactory.updateActiveTraceConfig(traceConfig);
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    for (int i = 0; i < 2 * maxNumberOfAttrs; i++) {
      spanBuilder.setAttribute("key" + i, i);
    }
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      ReadableAttributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.size()).isEqualTo(maxNumberOfAttrs);
      for (int i = 0; i < maxNumberOfAttrs; i++) {
        assertThat(attrs.get(longKey("key" + i))).isEqualTo(i);
      }
    } finally {
      span.end();
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  public void tooLargeAttributeValuesAreTruncated() {
    TraceConfig traceConfig =
        tracerSdkFactory
            .getActiveTraceConfig()
            .toBuilder()
            .setMaxLengthOfAttributeValues(10)
            .build();
    tracerSdkFactory.updateActiveTraceConfig(traceConfig);
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("builderStringNull", (String) null);
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
      ReadableAttributes attrs = span.toSpanData().getAttributes();
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
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  void addAttributes_OnlyViaSampler() {
    TraceConfig traceConfig =
        tracerSdkFactory
            .getActiveTraceConfig()
            .toBuilder()
            .setSampler(Samplers.traceIdRatioBased(1))
            .build();
    tracerSdkFactory.updateActiveTraceConfig(traceConfig);
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getAttributes().size()).isEqualTo(1);
      assertThat(span.toSpanData().getAttributes().get(Samplers.SAMPLING_PROBABILITY)).isEqualTo(1);
    } finally {
      span.end();
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  void recordEvents_default() {
    Span span = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.isRecording()).isTrue();
    } finally {
      span.end();
    }
  }

  @Test
  void kind_default() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(Kind.INTERNAL);
    } finally {
      span.end();
    }
  }

  @Test
  void kind() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            tracerSdk.spanBuilder(SPAN_NAME).setSpanKind(Kind.CONSUMER).startSpan();
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(Kind.CONSUMER);
    } finally {
      span.end();
    }
  }

  @Test
  void sampler() {
    Span span =
        TestUtils.startSpanWithSampler(tracerSdkFactory, tracerSdk, SPAN_NAME, Samplers.alwaysOff())
            .startSpan();
    try {
      assertThat(span.getContext().isSampled()).isFalse();
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
            TestUtils.startSpanWithSampler(
                    tracerSdkFactory,
                    tracerSdk,
                    SPAN_NAME,
                    new Sampler() {
                      @Override
                      public SamplingResult shouldSample(
                          @Nullable SpanContext parentContext,
                          String traceId,
                          String name,
                          Kind spanKind,
                          ReadableAttributes attributes,
                          List<SpanData.Link> parentLinks) {
                        return new SamplingResult() {
                          @Override
                          public Decision getDecision() {
                            return Decision.RECORD_AND_SAMPLE;
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
                    },
                    Collections.singletonMap(samplerAttributeKey.getKey(), "none"))
                .startSpan();
    try {
      assertThat(span.getContext().isSampled()).isTrue();
      assertThat(span.toSpanData().getAttributes().get(samplerAttributeKey)).isNotNull();
    } finally {
      span.end();
    }
  }

  @Test
  void sampledViaParentLinks() {
    Span span =
        TestUtils.startSpanWithSampler(
                tracerSdkFactory, tracerSdk, SPAN_NAME, Samplers.traceIdRatioBased(0.0))
            .addLink(sampledSpanContext)
            .startSpan();
    try {
      assertThat(span.getContext().isSampled()).isFalse();
    } finally {
      if (span != null) {
        span.end();
      }
    }
  }

  @Test
  void noParent() {
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try (Scope ignored = tracerSdk.withSpan(parent)) {
      Span span = tracerSdk.spanBuilder(SPAN_NAME).setNoParent().startSpan();
      try {
        assertThat(span.getContext().getTraceIdAsHexString())
            .isNotEqualTo(parent.getContext().getTraceIdAsHexString());

        Span spanNoParent =
            tracerSdk
                .spanBuilder(SPAN_NAME)
                .setNoParent()
                .setParent(Context.current())
                .setNoParent()
                .startSpan();
        try {
          assertThat(span.getContext().getTraceIdAsHexString())
              .isNotEqualTo(parent.getContext().getTraceIdAsHexString());
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
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              tracerSdk
                  .spanBuilder(SPAN_NAME)
                  .setNoParent()
                  .setParent(TracingContextUtils.withSpan(parent, Context.current()))
                  .startSpan();
      try {
        assertThat(span.getContext().getTraceIdAsHexString())
            .isEqualTo(parent.getContext().getTraceIdAsHexString());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getContext().getSpanIdAsHexString());

        RecordEventsReadableSpan span2 =
            (RecordEventsReadableSpan)
                tracerSdk
                    .spanBuilder(SPAN_NAME)
                    .setNoParent()
                    .setParent(TracingContextUtils.withSpan(parent, Context.current()))
                    .startSpan();
        try {
          assertThat(span2.getContext().getTraceIdAsHexString())
              .isEqualTo(parent.getContext().getTraceIdAsHexString());
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
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {

      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              tracerSdk
                  .spanBuilder(SPAN_NAME)
                  .setNoParent()
                  .setParent(TracingContextUtils.withSpan(parent, Context.current()))
                  .startSpan();
      try {
        assertThat(span.getContext().getTraceIdAsHexString())
            .isEqualTo(parent.getContext().getTraceIdAsHexString());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getContext().getSpanIdAsHexString());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void parent_fromContext() {
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    Context context = TracingContextUtils.withSpan(parent, Context.current());
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              tracerSdk.spanBuilder(SPAN_NAME).setNoParent().setParent(context).startSpan();
      try {
        assertThat(span.getContext().getTraceIdAsHexString())
            .isEqualTo(parent.getContext().getTraceIdAsHexString());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getContext().getSpanIdAsHexString());
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
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {
      RecordEventsReadableSpan span;
      try (Scope scope = TracingContextUtils.currentContextWith(parent)) {
        span =
            (RecordEventsReadableSpan)
                tracerSdk.spanBuilder(SPAN_NAME).setParent(emptyContext).startSpan();
      }

      try {
        assertThat(span.getContext().getTraceIdAsHexString())
            .isNotEqualTo(parent.getContext().getTraceIdAsHexString());
        assertThat(span.toSpanData().getParentSpanId())
            .isNotEqualTo(parent.getContext().getSpanIdAsHexString());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void parentCurrentSpan() {
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try (Scope ignored = tracerSdk.withSpan(parent)) {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).startSpan();
      try {
        assertThat(span.getContext().getTraceIdAsHexString())
            .isEqualTo(parent.getContext().getTraceIdAsHexString());
        assertThat(span.toSpanData().getParentSpanId())
            .isEqualTo(parent.getContext().getSpanIdAsHexString());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  void parent_invalidContext() {
    Span parent = DefaultSpan.getInvalid();

    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            tracerSdk
                .spanBuilder(SPAN_NAME)
                .setParent(TracingContextUtils.withSpan(parent, Context.current()))
                .startSpan();
    try {
      assertThat(span.getContext().getTraceIdAsHexString())
          .isNotEqualTo(parent.getContext().getTraceIdAsHexString());
      assertFalse(SpanId.isValid(span.toSpanData().getParentSpanId()));
    } finally {
      span.end();
    }
  }

  @Test
  void startTimestamp_null() {
    assertThrows(
        IllegalArgumentException.class,
        () -> tracerSdk.spanBuilder(SPAN_NAME).setStartTimestamp(-1),
        "Negative startTimestamp");
  }

  @Test
  void parent_clockIsSame() {
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try (Scope scope = tracerSdk.withSpan(parent)) {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).startSpan();

      assertThat(span.getClock()).isSameAs(((RecordEventsReadableSpan) parent).getClock());
    } finally {
      parent.end();
    }
  }

  @Test
  void parentCurrentSpan_clockIsSame() {
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try (Scope ignored = tracerSdk.withSpan(parent)) {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).startSpan();

      assertThat(span.getClock()).isSameAs(((RecordEventsReadableSpan) parent).getClock());
    } finally {
      parent.end();
    }
  }
}
