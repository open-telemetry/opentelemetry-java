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

import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.util.Samplers;
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
  public void addLinkSpanContextAttributes_null() {
    thrown.expect(NullPointerException.class);
    tracer.spanBuilder(SPAN_NAME).addLink(null, null);
  }

  @Test
  public void recordEvents_default() {
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).startSpan();
    assertThat(span.isRecordingEvents()).isTrue();
  }

  @Test
  public void recordEvents_neverSample() {
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl)
            tracer.spanBuilder(SPAN_NAME).setSampler(Samplers.neverSample()).startSpan();
    assertThat(span.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void kind_default() {
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).startSpan();
    assertThat(span.getKind()).isEqualTo(Kind.INTERNAL);
  }

  @Test
  public void kind() {
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl)
            tracer.spanBuilder(SPAN_NAME).setSpanKind(Kind.CONSUMER).startSpan();
    assertThat(span.getKind()).isEqualTo(Kind.CONSUMER);
  }

  @Test
  public void sampler() {
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl)
            tracer.spanBuilder(SPAN_NAME).setSampler(Samplers.neverSample()).startSpan();
    assertThat(span.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void sampler_decisionAttributes() {
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl)
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
                          List<Span> parentLinks) {
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
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(span.toSpanProto().getAttributes().getAttributeMapMap())
        .containsKey("sampler-attribute");
  }

  @Test
  public void noParent() {
    RecordEventsReadableSpanImpl parent =
        (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).startSpan();
    Scope scope = tracer.withSpan(parent);
    try {
      RecordEventsReadableSpanImpl span =
          (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).setNoParent().startSpan();
      assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());

      span =
          (RecordEventsReadableSpanImpl)
              tracer
                  .spanBuilder(SPAN_NAME)
                  .setNoParent()
                  .setParent(parent)
                  .setNoParent()
                  .startSpan();
      assertThat(span.getContext().getTraceId()).isNotEqualTo(parent.getContext().getTraceId());
    } finally {
      scope.close();
    }
  }

  @Test
  public void noParent_override() {
    RecordEventsReadableSpanImpl parent =
        (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).startSpan();
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl)
            tracer.spanBuilder(SPAN_NAME).setNoParent().setParent(parent).startSpan();
    io.opentelemetry.proto.trace.v1.Span spanProto = span.toSpanProto();
    assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
    assertThat(SpanId.fromBytes(spanProto.getParentSpanId().toByteArray(), 0))
        .isEqualTo(parent.getContext().getSpanId());

    span =
        (RecordEventsReadableSpanImpl)
            tracer.spanBuilder(SPAN_NAME).setNoParent().setParent(parent.getContext()).startSpan();
    assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
  }

  @Test
  public void overrideNoParent_remoteParent() {
    RecordEventsReadableSpanImpl parent =
        (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).startSpan();
    RecordEventsReadableSpanImpl span =
        (RecordEventsReadableSpanImpl)
            tracer.spanBuilder(SPAN_NAME).setNoParent().setParent(parent.getContext()).startSpan();
    io.opentelemetry.proto.trace.v1.Span spanProto = span.toSpanProto();
    assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
    assertThat(SpanId.fromBytes(spanProto.getParentSpanId().toByteArray(), 0))
        .isEqualTo(parent.getContext().getSpanId());
  }

  @Test
  public void parentCurrentSpan() {
    RecordEventsReadableSpanImpl parent =
        (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).startSpan();
    Scope scope = tracer.withSpan(parent);
    try {
      RecordEventsReadableSpanImpl span =
          (RecordEventsReadableSpanImpl) tracer.spanBuilder(SPAN_NAME).startSpan();
      io.opentelemetry.proto.trace.v1.Span spanProto = span.toSpanProto();
      assertThat(span.getContext().getTraceId()).isEqualTo(parent.getContext().getTraceId());
      assertThat(SpanId.fromBytes(spanProto.getParentSpanId().toByteArray(), 0))
          .isEqualTo(parent.getContext().getSpanId());
    } finally {
      scope.close();
    }
  }
}
