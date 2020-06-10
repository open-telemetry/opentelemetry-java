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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpanBuilderSdk}. */
@RunWith(JUnit4.class)
public class SpanBuilderSdkTest {
  private static final String SPAN_NAME = "span_name";
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          new TraceId(1000, 1000),
          new SpanId(3000),
          TraceFlags.builder().setIsSampled(true).build(),
          TraceState.getDefault());

  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final TracerSdk tracerSdk = tracerSdkFactory.get("SpanBuilderSdkTest");

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void setSpanKind_null() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).setSpanKind(null);
  }

  @Test
  public void setParent_null() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).setParent((Span) null);
  }

  @Test
  public void setRemoteParent_null() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).setParent((SpanContext) null);
  }

  @Test
  public void addLink() {
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
  public void truncateLink() {
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
  public void truncateLinkAttributes() {
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
  public void addLink_NoEffectAfterStartSpan() {
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
  public void addLink_null() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink((io.opentelemetry.trace.Link) null);
  }

  @Test
  public void addLinkSpanContext_null() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink((SpanContext) null);
  }

  @Test
  public void addLinkSpanContextAttributes_nullContext() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink(null, Attributes.empty());
  }

  @Test
  public void addLinkSpanContextAttributes_nullAttributes() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink(DefaultSpan.getInvalid().getContext(), null);
  }

  @Test
  public void setAttribute() {
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
      Attributes attrs = spanData.getAttributes();
      assertThat(attrs.size()).isEqualTo(5);
      assertThat(TestUtils.findByKey(attrs, "string"))
          .isEqualTo(AttributeValue.stringAttributeValue("value"));
      assertThat(TestUtils.findByKey(attrs, "long"))
          .isEqualTo(AttributeValue.longAttributeValue(12345L));
      assertThat(TestUtils.findByKey(attrs, "double"))
          .isEqualTo(AttributeValue.doubleAttributeValue(0.12345));
      assertThat(TestUtils.findByKey(attrs, "boolean"))
          .isEqualTo(AttributeValue.booleanAttributeValue(true));
      assertThat(TestUtils.findByKey(attrs, "stringAttribute"))
          .isEqualTo(AttributeValue.stringAttributeValue("attrvalue"));
      assertThat(spanData.getTotalAttributeCount()).isEqualTo(5);
    } finally {
      span.end();
    }
  }

  @Test
  public void setAttribute_afterEnd() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("string", "value");
    spanBuilder.setAttribute("long", 12345L);
    spanBuilder.setAttribute("double", .12345);
    spanBuilder.setAttribute("boolean", true);
    spanBuilder.setAttribute("stringAttribute", AttributeValue.stringAttributeValue("attrvalue"));

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      Attributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.size()).isEqualTo(5);
      assertThat(TestUtils.findByKey(attrs, "string"))
          .isEqualTo(AttributeValue.stringAttributeValue("value"));
      assertThat(TestUtils.findByKey(attrs, "long"))
          .isEqualTo(AttributeValue.longAttributeValue(12345L));
      assertThat(TestUtils.findByKey(attrs, "double"))
          .isEqualTo(AttributeValue.doubleAttributeValue(.12345));
      assertThat(TestUtils.findByKey(attrs, "boolean"))
          .isEqualTo(AttributeValue.booleanAttributeValue(true));
      assertThat(TestUtils.findByKey(attrs, "stringAttribute"))
          .isEqualTo(AttributeValue.stringAttributeValue("attrvalue"));
    } finally {
      span.end();
    }

    span.setAttribute("string2", "value");
    span.setAttribute("long2", 12345L);
    span.setAttribute("double2", .12345);
    span.setAttribute("boolean2", true);
    span.setAttribute("stringAttribute2", AttributeValue.stringAttributeValue("attrvalue"));

    Attributes attrs = span.toSpanData().getAttributes();
    assertThat(attrs.size()).isEqualTo(5);
    assertThat(TestUtils.findByKey(attrs, "string2")).isNull();
    assertThat(TestUtils.findByKey(attrs, "long2")).isNull();
    assertThat(TestUtils.findByKey(attrs, "double2")).isNull();
    assertThat(TestUtils.findByKey(attrs, "boolean2")).isNull();
    assertThat(TestUtils.findByKey(attrs, "stringAttribute2")).isNull();
  }

  @Test
  public void setAttribute_emptyArrayAttributeValue() {
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
  public void setAttribute_nullStringValue() {
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
  public void setAttribute_onlyNullStringValue() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("nullStringAttributeValue", AttributeValue.stringAttributeValue(null));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().isEmpty()).isTrue();
  }

  @Test
  public void setAttribute_NoEffectAfterStartSpan() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("key1", "value1");
    spanBuilder.setAttribute("key2", "value2");
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();

    Attributes beforeAttributes = span.toSpanData().getAttributes();
    assertThat(beforeAttributes.size()).isEqualTo(2);
    assertThat(TestUtils.findByKey(beforeAttributes, "key1"))
        .isEqualTo(AttributeValue.stringAttributeValue("value1"));
    assertThat(TestUtils.findByKey(beforeAttributes, "key2"))
        .isEqualTo(AttributeValue.stringAttributeValue("value2"));

    spanBuilder.setAttribute("key3", "value3");

    Attributes afterAttributes = span.toSpanData().getAttributes();
    assertThat(afterAttributes.size()).isEqualTo(2);
    assertThat(TestUtils.findByKey(afterAttributes, "key1"))
        .isEqualTo(AttributeValue.stringAttributeValue("value1"));
    assertThat(TestUtils.findByKey(afterAttributes, "key2"))
        .isEqualTo(AttributeValue.stringAttributeValue("value2"));
  }

  @Test
  public void setAttribute_nullAttributeValue() {
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
  public void setAttribute_nullAttributeValue_afterEnd() {
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
  public void droppingAttributes() {
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
      Attributes attrs = span.toSpanData().getAttributes();
      assertThat(attrs.size()).isEqualTo(maxNumberOfAttrs);
      for (int i = 0; i < maxNumberOfAttrs; i++) {
        assertThat(TestUtils.findByKey(attrs, "key" + i))
            .isEqualTo(AttributeValue.longAttributeValue(i));
      }
    } finally {
      span.end();
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  public void addAttributes_OnlyViaSampler() {
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
      assertThat(
              TestUtils.findByKey(
                  span.toSpanData().getAttributes(), Samplers.SAMPLING_PROBABILITY.key()))
          .isEqualTo(AttributeValue.doubleAttributeValue(1));
    } finally {
      span.end();
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  public void recordEvents_default() {
    Span span = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.isRecording()).isTrue();
    } finally {
      span.end();
    }
  }

  @Test
  public void kind_default() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.toSpanData().getKind()).isEqualTo(Kind.INTERNAL);
    } finally {
      span.end();
    }
  }

  @Test
  public void kind() {
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
  public void sampler() {
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
  public void sampler_decisionAttributes() {
    final String samplerAttributeName = "sampler-attribute";
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            TestUtils.startSpanWithSampler(
                    tracerSdkFactory,
                    tracerSdk,
                    SPAN_NAME,
                    new Sampler() {
                      @Override
                      public Decision shouldSample(
                          @Nullable SpanContext parentContext,
                          TraceId traceId,
                          String name,
                          Span.Kind spanKind,
                          Map<String, AttributeValue> attributes,
                          List<io.opentelemetry.trace.Link> parentLinks) {
                        return new Decision() {
                          @Override
                          public boolean isSampled() {
                            return true;
                          }

                          @Override
                          public Map<String, AttributeValue> getAttributes() {
                            Map<String, AttributeValue> attributes = new LinkedHashMap<>();
                            attributes.put(
                                samplerAttributeName, AttributeValue.stringAttributeValue("bar"));
                            return attributes;
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
      assertThat(TestUtils.findByKey(span.toSpanData().getAttributes(), samplerAttributeName))
          .isNotNull();
    } finally {
      span.end();
    }
  }

  @Test
  public void sampledViaParentLinks() {
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
  public void noParent() {
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
  public void noParent_override() {
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
  public void overrideNoParent_remoteParent() {
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
  public void parentCurrentSpan() {
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
  public void parent_invalidContext() {
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
  public void startTimestamp_null() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Negative startTimestamp");
    tracerSdk.spanBuilder(SPAN_NAME).setStartTimestamp(-1);
  }

  @Test
  public void parent_clockIsSame() {
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).setParent(parent).startSpan();

      assertThat(span.getClock()).isSameInstanceAs(((RecordEventsReadableSpan) parent).getClock());
    } finally {
      parent.end();
    }
  }

  @Test
  public void parentCurrentSpan_clockIsSame() {
    Span parent = tracerSdk.spanBuilder(SPAN_NAME).startSpan();
    try (Scope ignored = tracerSdk.withSpan(parent)) {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).startSpan();

      assertThat(span.getClock()).isSameInstanceAs(((RecordEventsReadableSpan) parent).getClock());
    } finally {
      parent.end();
    }
  }
}
