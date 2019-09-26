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

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.sdk.trace.samplers.ProbabilitySampler;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import io.opentelemetry.trace.util.Links;
import io.opentelemetry.trace.util.Samplers;
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
          Tracestate.getDefault());

  private final TracerSdk tracer = new TracerSdk();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void setSpanKind_null() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).setSpanKind(null);
  }

  @Test
  public void setSampler_null() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).setSampler(null);
  }

  @Test
  public void setParent_null() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).setParent((Span) null);
  }

  @Test
  public void setRemoteParent_null() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).setParent((SpanContext) null);
  }

  @Test
  public void addLink() {
    // Verify methods do not crash.
    Span.Builder spanBuilder = tracer.spanBuilder(SPAN_NAME);
    spanBuilder.addLink(Links.create(DefaultSpan.getInvalid().getContext()));
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext());
    spanBuilder.addLink(
        DefaultSpan.getInvalid().getContext(), Collections.<String, AttributeValue>emptyMap());

    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.getLinks()).hasSize(3);
    } finally {
      span.end();
    }
  }

  @Test
  public void truncateLink() {
    final int maxNumberOfLinks = 8;
    final Link link = Links.create(DefaultSpan.getInvalid().getContext());
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder().setMaxNumberOfLinks(maxNumberOfLinks).build();
    tracer.updateActiveTraceConfig(traceConfig);
    // Verify methods do not crash.
    Span.Builder spanBuilder = tracer.spanBuilder(SPAN_NAME);
    for (int i = 0; i < 2 * maxNumberOfLinks; i++) {
      spanBuilder.addLink(link);
    }
    RecordEventsReadableSpan span = (RecordEventsReadableSpan) spanBuilder.startSpan();
    try {
      assertThat(span.getDroppedLinksCount()).isEqualTo(maxNumberOfLinks);
      assertThat(span.getLinks().size()).isEqualTo(maxNumberOfLinks);
      for (int i = 0; i < maxNumberOfLinks; i++) {
        assertThat(span.getLinks().get(i)).isEqualTo(SpanData.Link.create(span.getContext()));
      }
    } finally {
      span.end();
      tracer.updateActiveTraceConfig(TraceConfig.getDefault());
    }
  }

  @Test
  public void addLink_null() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).addLink((Link) null);
  }

  @Test
  public void addLinkSpanContext_null() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).addLink((SpanContext) null);
  }

  @Test
  public void addLinkSpanContextAttributes_nullContext() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).addLink(null, Collections.<String, AttributeValue>emptyMap());
  }

  @Test
  public void addLinkSpanContextAttributes_nullAttributes() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).addLink(DefaultSpan.getInvalid().getContext(), null);
  }

  @Test
  public void recordEvents_default() {
    Span span = tracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.isRecordingEvents()).isTrue();
    } finally {
      span.end();
    }
  }

  @Test
  public void recordEvents_neverSample() {
    Span span = tracer.spanBuilder(SPAN_NAME).setSampler(Samplers.neverSample()).startSpan();
    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isFalse();
    } finally {
      span.end();
    }
  }

  @Test
  public void notSampledButRecordingEvents() {
    Span span =
        tracer
            .spanBuilder(SPAN_NAME)
            .setSampler(Samplers.neverSample())
            .setRecordEvents(true)
            .startSpan();
    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isFalse();
      assertThat(span.isRecordingEvents()).isTrue();
    } finally {
      span.end();
    }
  }

  @Test
  public void kind_default() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan) tracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      assertThat(span.getKind()).isEqualTo(Kind.INTERNAL);
    } finally {
      span.end();
    }
  }

  @Test
  public void kind() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            tracer.spanBuilder(SPAN_NAME).setSpanKind(Kind.CONSUMER).startSpan();
    try {
      assertThat(span.getKind()).isEqualTo(Kind.CONSUMER);
    } finally {
      span.end();
    }
  }

  @Test
  public void sampler() {
    Span span = tracer.spanBuilder(SPAN_NAME).setSampler(Samplers.neverSample()).startSpan();

    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isFalse();
    } finally {
      span.end();
    }
  }

  @Test
  public void sampler_decisionAttributes() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            tracer
                .spanBuilder(SPAN_NAME)
                .setSampler(
                    new Sampler() {
                      @Override
                      public Decision shouldSample(
                          @Nullable SpanContext parentContext,
                          @Nullable Boolean hasRemoteParent,
                          TraceId traceId,
                          SpanId spanId,
                          String name,
                          List<Link> parentLinks) {
                        return new Decision() {
                          @Override
                          public boolean isSampled() {
                            return true;
                          }

                          @Override
                          public Map<String, AttributeValue> attributes() {
                            Map<String, AttributeValue> attributes = new LinkedHashMap<>();
                            attributes.put(
                                "sampler-attribute", AttributeValue.stringAttributeValue("bar"));
                            return attributes;
                          }
                        };
                      }

                      @Override
                      public String getDescription() {
                        return "test sampler";
                      }
                    })
                .startSpan();
    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isTrue();
      assertThat(span.getAttributes()).containsKey("sampler-attribute");
    } finally {
      span.end();
    }
  }

  @Test
  public void sampledViaParentLinks() {
    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            tracer
                .spanBuilder(SPAN_NAME)
                .setSampler(ProbabilitySampler.create(0.0))
                .addLink(Links.create(sampledSpanContext))
                .startSpan();
    try {
      assertThat(span.getContext().getTraceFlags().isSampled()).isTrue();
    } finally {
      span.end();
    }
  }

  @Test
  public void noParent() {
    Span parent = tracer.spanBuilder(SPAN_NAME).startSpan();
    Scope scope = tracer.withSpan(parent);
    try {
      Span span = tracer.spanBuilder(SPAN_NAME).setNoParent().startSpan();
      try {
        assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());

        Span spanNoParent =
            tracer.spanBuilder(SPAN_NAME).setNoParent().setParent(parent).setNoParent().startSpan();
        try {
          assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());
        } finally {
          spanNoParent.end();
        }
      } finally {
        span.end();
      }
    } finally {
      scope.close();
      parent.end();
    }
  }

  @Test
  public void noParent_override() {
    Span parent = tracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              tracer.spanBuilder(SPAN_NAME).setNoParent().setParent(parent).startSpan();
      try {
        assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
        assertThat(span.getParentSpanId()).isEqualTo(parent.getContext().getSpanId());

        RecordEventsReadableSpan span2 =
            (RecordEventsReadableSpan)
                tracer
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
    Span parent = tracer.spanBuilder(SPAN_NAME).startSpan();
    try {

      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan)
              tracer
                  .spanBuilder(SPAN_NAME)
                  .setNoParent()
                  .setParent(parent.getContext())
                  .startSpan();
      try {
        assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
        assertThat(span.getParentSpanId()).isEqualTo(parent.getContext().getSpanId());
      } finally {
        span.end();
      }
    } finally {
      parent.end();
    }
  }

  @Test
  public void parentCurrentSpan() {
    Span parent = tracer.spanBuilder(SPAN_NAME).startSpan();
    Scope scope = tracer.withSpan(parent);
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracer.spanBuilder(SPAN_NAME).startSpan();
      try {
        assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
        assertThat(span.getParentSpanId()).isEqualTo(parent.getContext().getSpanId());
      } finally {
        span.end();
      }
    } finally {
      scope.close();
      parent.end();
    }
  }

  @Test
  public void parent_invalidContext() {
    Span parent = DefaultSpan.getInvalid();

    RecordEventsReadableSpan span =
        (RecordEventsReadableSpan)
            tracer.spanBuilder(SPAN_NAME).setParent(parent.getContext()).startSpan();
    try {
      assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());
      assertFalse(span.getParentSpanId().isValid());
    } finally {
      span.end();
    }
  }

  @Test
  public void parent_timestampConverter() {
    Span parent = tracer.spanBuilder(SPAN_NAME).startSpan();
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracer.spanBuilder(SPAN_NAME).setParent(parent).startSpan();

      assertThat(span.getTimestampConverter())
          .isEqualTo(((RecordEventsReadableSpan) parent).getTimestampConverter());
    } finally {
      parent.end();
    }
  }

  @Test
  public void parentCurrentSpan_timestampConverter() {
    Span parent = tracer.spanBuilder(SPAN_NAME).startSpan();
    Scope scope = tracer.withSpan(parent);
    try {
      RecordEventsReadableSpan span =
          (RecordEventsReadableSpan) tracer.spanBuilder(SPAN_NAME).startSpan();

      assertThat(span.getTimestampConverter())
          .isEqualTo(((RecordEventsReadableSpan) parent).getTimestampConverter());
    } finally {
      scope.close();
      parent.end();
    }
  }
}
