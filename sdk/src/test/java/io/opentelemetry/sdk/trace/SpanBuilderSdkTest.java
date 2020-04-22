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
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Link;
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
    spanBuilder.addLink(SpanData.Link.create(DefaultSpan.getInvalid().getContext()));
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext());
    spanBuilder.addLink(
        DefaultSpan.getInvalid().getContext(), Collections.<String, AttributeValue>emptyMap());

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
      List<SpanData.Link> links = spanData.getLinks();
      assertThat(links.size()).isEqualTo(maxNumberOfLinks);
      for (int i = 0; i < maxNumberOfLinks; i++) {
        assertThat(links.get(i)).isEqualTo(SpanData.Link.create(sampledSpanContext));
        assertThat(spanData.getTotalRecordedLinks()).isEqualTo(2 * maxNumberOfLinks);
      }
    } finally {
      span.end();
      tracerSdkFactory.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  public void addLink_null() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink((Link) null);
  }

  @Test
  public void addLinkSpanContext_null() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink((SpanContext) null);
  }

  @Test
  public void addLinkSpanContextAttributes_nullContext() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink(null, Collections.<String, AttributeValue>emptyMap());
  }

  @Test
  public void addLinkSpanContextAttributes_nullAttributes() {
    thrown.expect(NullPointerException.class);
    tracerSdk.spanBuilder(SPAN_NAME).addLink(DefaultSpan.getInvalid().getContext(), null);
  }

  @Test
  public void setAttribute() {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("string", "value");
    spanBuilder.setAttribute("long", 12345L);
    spanBuilder.setAttribute("double", .12345);
    spanBuilder.setAttribute("boolean", true);
    spanBuilder.setAttribute("stringAttribute", AttributeValue.stringAttributeValue("attrvalue"));

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      Map<String, AttributeValue> attrs = span.toSpanData().getAttributes();
      assertThat(attrs).hasSize(5);
      assertThat(attrs.get("string")).isEqualTo(AttributeValue.stringAttributeValue("value"));
      assertThat(attrs.get("long")).isEqualTo(AttributeValue.longAttributeValue(12345L));
      assertThat(attrs.get("double")).isEqualTo(AttributeValue.doubleAttributeValue(.12345));
      assertThat(attrs.get("boolean")).isEqualTo(AttributeValue.booleanAttributeValue(true));
      assertThat(attrs.get("stringAttribute"))
          .isEqualTo(AttributeValue.stringAttributeValue("attrvalue"));
    } finally {
      span.end();
    }
  }

  @Test
  public void setAttribute_emptyArrayAttributeValue() throws Exception {
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
  public void setAttribute_nullStringValue() throws Exception {
    Span.Builder spanBuilder = tracerSdk.spanBuilder(SPAN_NAME);
    spanBuilder.setAttribute("emptyString", "");
    spanBuilder.setAttribute("nullString", (String) null);
    spanBuilder.setAttribute("nullStringAttributeValue", AttributeValue.stringAttributeValue(null));
    spanBuilder.setAttribute("emptyStringAttributeValue", AttributeValue.stringAttributeValue(""));
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    assertThat(span.toSpanData().getAttributes().size()).isEqualTo(2);
    spanBuilder.setAttribute("emptyString", (String) null);
    spanBuilder.setAttribute("emptyStringAttributeValue", (String) null);
    assertThat(span.toSpanData().getAttributes()).isEmpty();
  }

  @Test
  public void setAttribute_nullAttributeValue() throws Exception {
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
    spanBuilder.setAttribute("emptyString", (AttributeValue) null);
    spanBuilder.setAttribute("emptyStringAttributeValue", (AttributeValue) null);
    spanBuilder.setAttribute("longAttribute", (AttributeValue) null);
    spanBuilder.setAttribute("boolAttribute", (AttributeValue) null);
    spanBuilder.setAttribute("doubleAttribute", (AttributeValue) null);
    spanBuilder.setAttribute("stringArrayAttribute", (AttributeValue) null);
    spanBuilder.setAttribute("boolArrayAttribute", (AttributeValue) null);
    spanBuilder.setAttribute("longArrayAttribute", (AttributeValue) null);
    spanBuilder.setAttribute("doubleArrayAttribute", (AttributeValue) null);
    assertThat(span.toSpanData().getAttributes()).isEmpty();
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
      Map<String, AttributeValue> attrs = span.toSpanData().getAttributes();
      assertThat(attrs.size()).isEqualTo(maxNumberOfAttrs);
      for (int i = 0; i < maxNumberOfAttrs; i++) {
        assertThat(attrs.get("key" + (i + maxNumberOfAttrs)))
            .isEqualTo(AttributeValue.longAttributeValue(i + maxNumberOfAttrs));
      }
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
                          SpanId spanId,
                          String name,
                          Span.Kind spanKind,
                          Map<String, AttributeValue> attributes,
                          List<Link> parentLinks) {
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
                    Collections.<String, AttributeValue>singletonMap(
                        samplerAttributeName, AttributeValue.stringAttributeValue("none")))
                .startSpan();
    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isTrue();
      assertThat(span.toSpanData().getAttributes()).containsKey(samplerAttributeName);
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
    try (Scope scope = tracerSdk.withSpan(parent)) {
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
    try (Scope scope = tracerSdk.withSpan(parent)) {
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
    try (Scope scope = tracerSdk.withSpan(parent)) {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracerSdk.spanBuilder(SPAN_NAME).startSpan();

      assertThat(span.getClock()).isSameInstanceAs(((RecordEventsReadableSpan) parent).getClock());
    } finally {
      parent.end();
    }
  }
}
