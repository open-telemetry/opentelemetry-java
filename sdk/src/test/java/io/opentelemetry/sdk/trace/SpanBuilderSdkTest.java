/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Context;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
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
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanBuilderSdk}. */
class SpanBuilderSdkTest {
  private static final String SPAN_NAME = "span_name";
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          new TraceId(1000, 1000),
          new SpanId(3000),
          TraceFlags.builder().setIsSampled(true).build(),
          TraceState.getDefault());

  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final TracerSdk tracerSdk = tracerSdkFactory.get("SpanBuilderSdkTest");

  @Test
  void setSpanKind_null() {
    assertThrows(
        NullPointerException.class, () -> tracerSdk.spanBuilder(SPAN_NAME).setSpanKind(null));
  }

  @Test
  void setParent_null() {
    assertThrows(
        NullPointerException.class, () -> tracerSdk.spanBuilder(SPAN_NAME).setParent((Span) null));
  }

  @Test
  void setRemoteParent_null() {
    assertThrows(
        NullPointerException.class,
        () -> tracerSdk.spanBuilder(SPAN_NAME).setParent((SpanContext) null));
  }

  @Test
  void addLink() {
    // Verify methods do not crash.
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.addLink(Link.create(DefaultSpan.getInvalid().getContext()));
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext());
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext(), Attributes.empty());

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getLinks()).hasSize(3);
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
      List<Link> links = spanData.getLinks();
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
            "key0", AttributeValue.stringAttributeValue("str"),
            "key1", AttributeValue.stringAttributeValue("str"),
            "key2", AttributeValue.stringAttributeValue("str"));
    spanBuilder.addLink(sampledSpanContext, attributes);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getLinks())
          .containsExactly(
              Link.create(
                  sampledSpanContext,
                  Attributes.of("key0", AttributeValue.stringAttributeValue("str")),
                  3));
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
              new TraceId(2000, 2000),
              new SpanId(4000),
              TraceFlags.builder().setIsSampled(true).build(),
              TraceState.getDefault()));
      assertThat(span.toSpanData().getLinks())
          .containsExactly(Link.create(sampledSpanContext, Attributes.empty()));
    } finally {
      span.end();
    }
  }

  @Test
  void addLink_null() {
    assertThrows(
        NullPointerException.class,
        () -> tracerSdk.spanBuilder(SPAN_NAME).addLink((io.opentelemetry.trace.Link) null));
  }

  @Test
  void addLinkSpanContext_null() {
    assertThrows(
        NullPointerException.class,
        () -> tracerSdk.spanBuilder(SPAN_NAME).addLink((SpanContext) null));
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
            .setAttribute("stringAttribute", AttributeValue.stringAttributeValue("attrvalue"));

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      SpanData spanData = span.toSpanData();
      ReadableAttributes attrs = spanData.getAttributes();
      assertThat(attrs.size()).isEqualTo(5);
      assertThat(attrs.get("string")).isEqualTo(AttributeValue.stringAttributeValue("value"));
      assertThat(attrs.get("long")).isEqualTo(AttributeValue.longAttributeValue(12345L));
      assertThat(attrs.get("double")).isEqualTo(AttributeValue.doubleAttributeValue(0.12345));
      assertThat(attrs.get("boolean")).isEqualTo(AttributeValue.booleanAttributeValue(true));
      assertThat(attrs.get("stringAttribute"))
          .isEqualTo(AttributeValue.stringAttributeValue("attrvalue"));
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
    spanBuilder.setAttribute("stringAttribute", AttributeValue.stringAttributeValue("attrvalue"));

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      ReadableAttributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.size()).isEqualTo(5);
      assertThat(attrs.get("string")).isEqualTo(AttributeValue.stringAttributeValue("value"));
      assertThat(attrs.get("long")).isEqualTo(AttributeValue.longAttributeValue(12345L));
      assertThat(attrs.get("double")).isEqualTo(AttributeValue.doubleAttributeValue(.12345));
      assertThat(attrs.get("boolean")).isEqualTo(AttributeValue.booleanAttributeValue(true));
      assertThat(attrs.get("stringAttribute"))
          .isEqualTo(AttributeValue.stringAttributeValue("attrvalue"));
    } finally {
      span.end();
    }

    span.setAttribute("string2", "value");
    span.setAttribute("long2", 12345L);
    span.setAttribute("double2", .12345);
    span.setAttribute("boolean2", true);
    span.setAttribute("stringAttribute2", AttributeValue.stringAttributeValue("attrvalue"));

    ReadableAttributes attrs = span.toSpanData().getAttributes();
    assertThat(attrs.size()).isEqualTo(5);
    assertThat(attrs.get("string2")).isNull();
    assertThat(attrs.get("long2")).isNull();
    assertThat(attrs.get("double2")).isNull();
    assertThat(attrs.get("boolean2")).isNull();
    assertThat(attrs.get("stringAttribute2")).isNull();
  }

  @Test
  void setAttribute_emptyArrayAttributeValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute(
        "stringArrayAttribute", AttributeValue.arrayAttributeValue(new String[0]));
    spanBuilder.setAttribute(
        "boolArrayAttribute", AttributeValue.arrayAttributeValue(new Boolean[0]));
    spanBuilder.setAttribute("longArrayAttribute", AttributeValue.arrayAttributeValue(new Long[0]));
    spanBuilder.setAttribute(
        "doubleArrayAttribute", AttributeValue.arrayAttributeValue(new Double[0]));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(4);
  }

  @Test
  void setAttribute_nullStringValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute("nullString", (String) null);
    spanBuilder.setAttribute("nullStringAttributeValue", AttributeValue.stringAttributeValue(null));
    spanBuilder.setAttribute("emptyStringAttributeValue", AttributeValue.stringAttributeValue(""));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
    span.setAttribute("emptyString", (String) null);
    span.setAttribute("emptyStringAttributeValue", (String) null);
    assertThat(span.toSpanData().getAttributes().isEmpty()).isTrue();
  }

  @Test
  void setAttribute_onlyNullStringValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("nullStringAttributeValue", AttributeValue.stringAttributeValue(null));
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
    assertThat(beforeAttributes.get("key1"))
        .isEqualTo(AttributeValue.stringAttributeValue("value1"));
    assertThat(beforeAttributes.get("key2"))
        .isEqualTo(AttributeValue.stringAttributeValue("value2"));

    spanBuilder.setAttribute("key3", "value3");

    ReadableAttributes afterAttributes = span.toSpanData().getAttributes();
    assertThat(afterAttributes.size()).isEqualTo(2);
    assertThat(afterAttributes.get("key1"))
        .isEqualTo(AttributeValue.stringAttributeValue("value1"));
    assertThat(afterAttributes.get("key2"))
        .isEqualTo(AttributeValue.stringAttributeValue("value2"));
  }

  @Test
  void setAttribute_nullAttributeValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute("nullString", (AttributeValue) null);
    spanBuilder.setAttribute("nullStringAttributeValue", AttributeValue.stringAttributeValue(null));
    spanBuilder.setAttribute("emptyStringAttributeValue", AttributeValue.stringAttributeValue(""));
    spanBuilder.setAttribute("longAttribute", 0L);
    spanBuilder.setAttribute("boolAttribute", false);
    spanBuilder.setAttribute("doubleAttribute", 0.12345f);
    spanBuilder.setAttribute("stringArrayAttribute", AttributeValue.arrayAttributeValue("", null));
    spanBuilder.setAttribute("boolArrayAttribute", AttributeValue.arrayAttributeValue(true, null));
    spanBuilder.setAttribute(
        "longArrayAttribute", AttributeValue.arrayAttributeValue(12345L, null));
    spanBuilder.setAttribute(
        "doubleArrayAttribute", AttributeValue.arrayAttributeValue(1.2345, null));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
    span.setAttribute("emptyString", (AttributeValue) null);
    span.setAttribute("emptyStringAttributeValue", (AttributeValue) null);
    span.setAttribute("longAttribute", (AttributeValue) null);
    span.setAttribute("boolAttribute", (AttributeValue) null);
    span.setAttribute("doubleAttribute", (AttributeValue) null);
    span.setAttribute("stringArrayAttribute", (AttributeValue) null);
    span.setAttribute("boolArrayAttribute", (AttributeValue) null);
    span.setAttribute("longArrayAttribute", (AttributeValue) null);
    span.setAttribute("doubleArrayAttribute", (AttributeValue) null);
    assertThat(span.toSpanData().getAttributes().isEmpty()).isTrue();
  }

  @Test
  void setAttribute_nullAttributeValue_afterEnd() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute("emptyStringAttributeValue", AttributeValue.stringAttributeValue(""));
    spanBuilder.setAttribute("longAttribute", 0L);
    spanBuilder.setAttribute("boolAttribute", false);
    spanBuilder.setAttribute("doubleAttribute", 0.12345f);
    spanBuilder.setAttribute("stringArrayAttribute", AttributeValue.arrayAttributeValue("", null));
    spanBuilder.setAttribute("boolArrayAttribute", AttributeValue.arrayAttributeValue(true, null));
    spanBuilder.setAttribute(
        "longArrayAttribute", AttributeValue.arrayAttributeValue(12345L, null));
    spanBuilder.setAttribute(
        "doubleArrayAttribute", AttributeValue.arrayAttributeValue(1.2345, null));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(9);
    span.end();
    span.setAttribute("emptyString", (AttributeValue) null);
    span.setAttribute("emptyStringAttributeValue", (AttributeValue) null);
    span.setAttribute("longAttribute", (AttributeValue) null);
    span.setAttribute("boolAttribute", (AttributeValue) null);
    span.setAttribute("doubleAttribute", (AttributeValue) null);
    span.setAttribute("stringArrayAttribute", (AttributeValue) null);
    span.setAttribute("boolArrayAttribute", (AttributeValue) null);
    span.setAttribute("longArrayAttribute", (AttributeValue) null);
    span.setAttribute("doubleArrayAttribute", (AttributeValue) null);
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
        assertThat(attrs.get("key" + i)).isEqualTo(AttributeValue.longAttributeValue(i));
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
        "builderStringLargeValue",
        AttributeValue.stringAttributeValue("very large string that we have to cut"));
    spanBuilder.setAttribute(
        "builderStringArray",
        AttributeValue.arrayAttributeValue("small", null, "very large string that we have to cut"));

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    span.setAttribute("spanStringSmall", "small");
    span.setAttribute("spanStringLarge", "very large string that we have to cut");
    span.setAttribute("spanLong", 42L);
    span.setAttribute(
        "spanStringLarge",
        AttributeValue.stringAttributeValue("very large string that we have to cut"));
    span.setAttribute(
        "spanStringArray",
        AttributeValue.arrayAttributeValue("small", null, "very large string that we have to cut"));

    try {
      ReadableAttributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.get("builderStringNull")).isEqualTo(null);
      assertThat(attrs.get("builderStringSmall"))
          .isEqualTo(AttributeValue.stringAttributeValue("small"));
      assertThat(attrs.get("builderStringLarge"))
          .isEqualTo(AttributeValue.stringAttributeValue("very large"));
      assertThat(attrs.get("builderLong")).isEqualTo(AttributeValue.longAttributeValue(42L));
      assertThat(attrs.get("builderStringLargeValue"))
          .isEqualTo(AttributeValue.stringAttributeValue("very large"));
      assertThat(attrs.get("builderStringArray"))
          .isEqualTo(AttributeValue.arrayAttributeValue("small", null, "very large"));

      assertThat(attrs.get("spanStringSmall"))
          .isEqualTo(AttributeValue.stringAttributeValue("small"));
      assertThat(attrs.get("spanStringLarge"))
          .isEqualTo(AttributeValue.stringAttributeValue("very large"));
      assertThat(attrs.get("spanLong")).isEqualTo(AttributeValue.longAttributeValue(42L));
      assertThat(attrs.get("spanStringLarge"))
          .isEqualTo(AttributeValue.stringAttributeValue("very large"));
      assertThat(attrs.get("spanStringArray"))
          .isEqualTo(AttributeValue.arrayAttributeValue("small", null, "very large"));
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
            .setSampler(Samplers.probability(1))
            .build();
    tracerSdkFactory.updateActiveTraceConfig(traceConfig);
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.toSpanData().getAttributes().size()).isEqualTo(1);
      assertThat(span.toSpanData().getAttributes().get(Samplers.SAMPLING_PROBABILITY.key()))
          .isEqualTo(AttributeValue.doubleAttributeValue(1));
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
      assertThat(span.getContext().getTraceFlags().isSampled()).isFalse();
    } finally {
      span.end();
    }
  }

  @Test
  void sampler_decisionAttributes() {
    final String samplerAttributeName = "sampler-attribute";
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
                          TraceId traceId,
                          String name,
                          Kind spanKind,
                          ReadableAttributes attributes,
                          List<io.opentelemetry.trace.Link> parentLinks) {
                        return new SamplingResult() {
                          @Override
                          public Decision getDecision() {
                            return Decision.RECORD_AND_SAMPLED;
                          }

                          @Override
                          public Attributes getAttributes() {
                            return Attributes.of(
                                samplerAttributeName, AttributeValue.stringAttributeValue("bar"));
                          }
                        };
                      }

                      @Override
                      public String getDescription() {
                        return "test sampler";
                      }
                    },
                    Collections.singletonMap(
                        samplerAttributeName, AttributeValue.stringAttributeValue("none")))
                .startSpan();
    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isTrue();
      assertThat(span.toSpanData().getAttributes().get(samplerAttributeName)).isNotNull();
    } finally {
      span.end();
    }
  }

  @Test
  void sampledViaParentLinks() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            TestUtils.startSpanWithSampler(
                    tracerSdkFactory, tracerSdk, SPAN_NAME, Samplers.probability(0.0))
                .addLink(sampledSpanContext)
                .startSpan();
    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isTrue();
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
        assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());

        Span spanNoParent =
            tracerSdk
                .spanBuilder(SPAN_NAME)
                .setNoParent()
                .setParent(parent)
                .setNoParent()
                .startSpan();
        try {
          assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());
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
              tracerSdk.spanBuilder(SPAN_NAME).setNoParent().setParent(parent).startSpan();
      try {
        assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId()).isEqualTo(parent.getContext().getSpanId());

        RecordEventsReadableSpan span2 =
            (RecordEventsReadableSpan)
                tracerSdk
                    .spanBuilder(SPAN_NAME)
                    .setNoParent()
                    .setParent(parent.getContext())
                    .startSpan();
        try {
          assertThat(span2.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
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
                  .setParent(parent.getContext())
                  .startSpan();
      try {
        assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId()).isEqualTo(parent.getContext().getSpanId());
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
        assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId()).isEqualTo(parent.getContext().getSpanId());
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
      RecordEventsReadableSpan span = null;
      try (Scope scope = TracingContextUtils.currentContextWith(parent)) {
        span =
            (RecordEventsReadableSpan)
                tracerSdk.spanBuilder(SPAN_NAME).setParent(emptyContext).startSpan();
      }

      try {
        assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId())
            .isNotEqualTo(parent.getContext().getSpanId());
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
        assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
        assertThat(span.toSpanData().getParentSpanId()).isEqualTo(parent.getContext().getSpanId());
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
            tracerSdk.spanBuilder(SPAN_NAME).setParent(parent.getContext()).startSpan();
    try {
      assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());
      assertFalse(span.toSpanData().getParentSpanId().isValid());
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
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).setParent(parent).startSpan();

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
