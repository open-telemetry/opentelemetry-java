/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.opentracingshim.TestUtils.getBaggageMap;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import io.opentracing.References;
import io.opentracing.noop.NoopSpan;
import io.opentracing.tag.Tags;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpanBuilderShimTest {

  private final SdkTracerProvider tracerSdkFactory = SdkTracerProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");

  private static final String SPAN_NAME = "Span";

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void default_values() {
    SpanShim span = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    try {
      SpanData spanData = ((ReadableSpan) span.getSpan()).toSpanData();
      assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
      assertThat(spanData.getStatus()).isEqualTo(StatusData.unset());
      assertThat(spanData.getSpanContext()).isNotEqualTo(SpanContext.getInvalid());
      assertThat(spanData.getKind()).isEqualTo(SpanKind.INTERNAL);
      assertThat(spanData.getAttributes().size()).isEqualTo(0);
      assertThat(spanData.getEvents()).hasSize(0);
      assertThat(spanData.getLinks()).hasSize(0);
    } finally {
      span.finish();
    }
  }

  @Test
  void parent_single() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    try {
      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).asChildOf(parentSpan).start();

      try {
        SpanData spanData = ((ReadableSpan) childSpan.getSpan()).toSpanData();
        assertThat(parentSpan.context().toSpanId()).isEqualTo(spanData.getParentSpanId());
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void noop_parent_span() {
    SpanShim childSpan =
        (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).asChildOf(NoopSpan.INSTANCE).start();
    try {
      SpanData spanData = ((ReadableSpan) childSpan.getSpan()).toSpanData();
      assertThat(SpanId.isValid(spanData.getParentSpanId())).isFalse();
    } finally {
      childSpan.finish();
    }
  }

  @Test
  void parent_multipleFollowsFrom() {
    SpanShim parentSpan1 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    SpanShim parentSpan2 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();

    try {
      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(tracer, SPAN_NAME)
                  .addReference(References.FOLLOWS_FROM, parentSpan1.context())
                  .addReference(References.FOLLOWS_FROM, parentSpan2.context())
                  .start();

      try {
        // If no parent of CHILD_OF type exists, use the first value as main parent.
        SpanData spanData = ((ReadableSpan) childSpan.getSpan()).toSpanData();
        assertThat(parentSpan1.context().toSpanId()).isEqualTo(spanData.getParentSpanId());
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan1.finish();
      parentSpan2.finish();
    }
  }

  @Test
  void parent_multipleDifferentRefType() {
    SpanShim parentSpan1 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    SpanShim parentSpan2 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    SpanShim parentSpan3 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();

    try {
      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(tracer, SPAN_NAME)
                  .addReference(References.FOLLOWS_FROM, parentSpan1.context())
                  .addReference(References.CHILD_OF, parentSpan2.context())
                  .addReference(References.CHILD_OF, parentSpan3.context())
                  .start();

      try {
        // The first parent with CHILD_OF becomes the direct parent (if any).
        SpanData spanData = ((ReadableSpan) childSpan.getSpan()).toSpanData();
        assertThat(parentSpan2.context().toSpanId()).isEqualTo(spanData.getParentSpanId());
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan1.finish();
      parentSpan2.finish();
      parentSpan3.finish();
    }
  }

  @Test
  void parent_multipleLinks() {
    SpanShim parentSpan1 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    SpanShim parentSpan2 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    SpanShim parentSpan3 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();

    try {
      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(tracer, SPAN_NAME)
                  .addReference(References.FOLLOWS_FROM, parentSpan1.context())
                  .addReference(References.CHILD_OF, parentSpan2.context())
                  .addReference(References.FOLLOWS_FROM, parentSpan3.context())
                  .start();

      try {
        SpanData spanData = ((ReadableSpan) childSpan.getSpan()).toSpanData();
        List<LinkData> links = spanData.getLinks();
        assertThat(links).hasSize(3);

        assertThat(links.get(0).getSpanContext()).isEqualTo(parentSpan1.getSpan().getSpanContext());
        assertThat(links.get(1).getSpanContext()).isEqualTo(parentSpan2.getSpan().getSpanContext());
        assertThat(links.get(2).getSpanContext()).isEqualTo(parentSpan3.getSpan().getSpanContext());
        assertThat(links.get(0).getAttributes().get(stringKey("opentracing.ref_type")))
            .isEqualTo("follows_from");
        assertThat(links.get(1).getAttributes().get(stringKey("opentracing.ref_type")))
            .isEqualTo("child_of");
        assertThat(links.get(2).getAttributes().get(stringKey("opentracing.ref_type")))
            .isEqualTo("follows_from");

      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan1.finish();
      parentSpan2.finish();
      parentSpan3.finish();
    }
  }

  @Test
  void parent_wrongRefType() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    try {
      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(tracer, SPAN_NAME)
                  .addReference("wrongreftype-value", parentSpan.context())
                  .start();

      try {
        // Incorrect typeref values get discarded.
        SpanData spanData = ((ReadableSpan) childSpan.getSpan()).toSpanData();
        assertThat(spanData.getParentSpanContext()).isEqualTo(SpanContext.getInvalid());
        assertThat(spanData.getLinks()).isEmpty();
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void baggage_parent() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertThat("value1").isEqualTo(childSpan.getBaggageItem("key1"));
        assertThat(getBaggageMap(parentSpan.context().baggageItems()))
            .isEqualTo(getBaggageMap(childSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void baggage_parentContext() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).asChildOf(parentSpan.context()).start();
      try {
        assertThat("value1").isEqualTo(childSpan.getBaggageItem("key1"));
        assertThat(getBaggageMap(parentSpan.context().baggageItems()))
            .isEqualTo(getBaggageMap(childSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void baggage_multipleParents() {
    SpanShim parentSpan1 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    SpanShim parentSpan2 = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    try {
      parentSpan1.setBaggageItem("key1", "value1");
      parentSpan2.setBaggageItem("key2", "value2");

      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(tracer, SPAN_NAME)
                  .addReference(References.FOLLOWS_FROM, parentSpan1.context())
                  .addReference(References.CHILD_OF, parentSpan2.context())
                  .start();
      try {
        assertThat(childSpan.getBaggageItem("key1")).isEqualTo("value1");
        assertThat(childSpan.getBaggageItem("key2")).isEqualTo("value2");
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan1.finish();
      parentSpan2.finish();
    }
  }

  @Test
  void baggage_spanWithInvalidSpan() {
    Baggage baggage = Baggage.builder().put("foo", "bar").build();
    SpanShim span = new SpanShim(Span.getInvalid(), baggage);

    SpanShim childSpan = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).asChildOf(span).start();
    assertThat(childSpan.getBaggage()).isEqualTo(baggage);
  }

  @Test
  void baggage_spanContextWithInvalidSpan() {
    Baggage baggage = Baggage.builder().put("foo", "bar").build();
    SpanContextShim spanContext = new SpanContextShim(Span.getInvalid().getSpanContext(), baggage);

    SpanShim childSpan =
        (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).asChildOf(spanContext).start();
    assertThat(childSpan.getBaggage()).isEqualTo(baggage);
  }

  @Test
  void parent_NullContextShim() {
    /* SpanContextShim is null until Span.context() or Span.getBaggageItem() are called.
     * Verify a null SpanContextShim in the parent is handled properly. */
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).start();
    try {
      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertThat(childSpan.context().baggageItems().iterator().hasNext()).isFalse();
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void withStartTimestamp() {
    long micros = 123447307984L;
    SpanShim spanShim =
        (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).withStartTimestamp(micros).start();
    SpanData spanData = ((ReadableSpan) spanShim.getSpan()).toSpanData();
    assertThat(spanData.getStartEpochNanos()).isEqualTo(micros * 1000L);
  }

  @Test
  void setAttribute_errorAsBoolean() {
    SpanShim span1 =
        (SpanShim)
            new SpanBuilderShim(tracer, SPAN_NAME).withTag(Tags.ERROR.getKey(), true).start();
    SpanShim span2 =
        (SpanShim)
            new SpanBuilderShim(tracer, SPAN_NAME).withTag(Tags.ERROR.getKey(), false).start();
    try {
      SpanData spanData1 = ((ReadableSpan) span1.getSpan()).toSpanData();
      assertThat(spanData1.getStatus()).isEqualTo(StatusData.error());

      SpanData spanData2 = ((ReadableSpan) span2.getSpan()).toSpanData();
      assertThat(spanData2.getStatus()).isEqualTo(StatusData.ok());
    } finally {
      span1.finish();
      span2.finish();
    }
  }

  @Test
  void setAttribute_errorAsString() {
    SpanShim span1 =
        (SpanShim)
            new SpanBuilderShim(tracer, SPAN_NAME).withTag(Tags.ERROR.getKey(), "tRuE").start();
    SpanShim span2 =
        (SpanShim)
            new SpanBuilderShim(tracer, SPAN_NAME).withTag(Tags.ERROR.getKey(), "FaLsE").start();
    try {
      SpanData spanData1 = ((ReadableSpan) span1.getSpan()).toSpanData();
      assertThat(spanData1.getStatus()).isEqualTo(StatusData.error());

      SpanData spanData2 = ((ReadableSpan) span2.getSpan()).toSpanData();
      assertThat(spanData2.getStatus()).isEqualTo(StatusData.ok());
    } finally {
      span1.finish();
      span2.finish();
    }
  }

  @Test
  void setAttribute_unrecognizedType() {
    SpanShim span =
        (SpanShim) new SpanBuilderShim(tracer, SPAN_NAME).withTag("foo", BigInteger.TEN).start();
    try {
      SpanData spanData = ((ReadableSpan) span.getSpan()).toSpanData();
      assertThat(spanData.getAttributes().size()).isEqualTo(1);
      assertThat(spanData.getAttributes().get(stringKey("foo"))).isEqualTo("10");
    } finally {
      span.finish();
    }
  }

  @Test
  void setAttributes_beforeSpanStart() {
    SdkTracerProvider tracerSdkFactory =
        SdkTracerProvider.builder().setSampler(SamplingPrioritySampler.INSTANCE).build();
    Tracer tracer = tracerSdkFactory.get("SpanShimTest");

    SpanBuilderShim spanBuilder1 = new SpanBuilderShim(tracer, SPAN_NAME);
    SpanBuilderShim spanBuilder2 = new SpanBuilderShim(tracer, SPAN_NAME);
    SpanShim span1 =
        (SpanShim)
            spanBuilder1
                .withTag(
                    SamplingPrioritySampler.SAMPLING_PRIORITY_TAG,
                    SamplingPrioritySampler.UNSAMPLED_VALUE)
                .start();
    SpanShim span2 =
        (SpanShim)
            spanBuilder2
                .withTag(
                    SamplingPrioritySampler.SAMPLING_PRIORITY_TAG,
                    SamplingPrioritySampler.SAMPLED_VALUE)
                .start();
    assertThat(span1.getSpan().isRecording()).isFalse();
    assertThat(span2.getSpan().isRecording()).isTrue();
    assertThat(span1.getSpan().getSpanContext().isSampled()).isFalse();
    assertThat(span2.getSpan().getSpanContext().isSampled()).isTrue();
  }

  static final class SamplingPrioritySampler implements Sampler {
    static final SamplingPrioritySampler INSTANCE = new SamplingPrioritySampler();
    static final String SAMPLING_PRIORITY_TAG = "sampling.priority";
    static final long UNSAMPLED_VALUE = 0;
    static final long SAMPLED_VALUE = 1;

    @Override
    public String getDescription() {
      return "SamplingPrioritySampler";
    }

    @Override
    public SamplingResult shouldSample(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {

      if (attributes.get(AttributeKey.longKey(SAMPLING_PRIORITY_TAG)) == UNSAMPLED_VALUE) {
        return SamplingResult.drop();
      }

      return SamplingResult.recordAndSample();
    }
  }
}
