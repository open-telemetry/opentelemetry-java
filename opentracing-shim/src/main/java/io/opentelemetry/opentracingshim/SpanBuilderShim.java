/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

final class SpanBuilderShim implements SpanBuilder {

  private static final Attributes CHILD_OF_ATTR =
      Attributes.of(
          SemanticAttributes.OPENTRACING_REF_TYPE,
          SemanticAttributes.OpentracingRefTypeValues.CHILD_OF);
  private static final Attributes FOLLOWS_FROM_ATTR =
      Attributes.of(
          SemanticAttributes.OPENTRACING_REF_TYPE,
          SemanticAttributes.OpentracingRefTypeValues.FOLLOWS_FROM);

  private final Tracer tracer;
  private final String spanName;

  // *All* parents are saved in this list.
  private List<SpanParentInfo> allParents = Collections.emptyList();
  private boolean ignoreActiveSpan;

  private final List<AttributeKey<?>> spanBuilderAttributeKeys = new ArrayList<>();
  private final List<Object> spanBuilderAttributeValues = new ArrayList<>();

  @Nullable private Boolean error;
  private long startTimestampMicros;

  SpanBuilderShim(Tracer tracer, String spanName) {
    this.tracer = tracer;
    this.spanName = spanName;
  }

  @Override
  public SpanBuilder asChildOf(Span parent) {
    SpanShim spanShim = ShimUtil.getSpanShim(parent);
    if (spanShim != null) {
      addReference(References.CHILD_OF, spanShim.context());
    }

    return this;
  }

  @Override
  public SpanBuilder asChildOf(SpanContext parent) {
    return addReference(References.CHILD_OF, parent);
  }

  @Override
  public SpanBuilder addReference(@Nullable String referenceType, SpanContext referencedContext) {
    SpanContextShim contextShim = ShimUtil.getContextShim(referencedContext);
    if (contextShim == null) {
      return this;
    }

    ReferenceType refType;
    if (References.CHILD_OF.equals(referenceType)) {
      refType = ReferenceType.CHILD_OF;
    } else if (References.FOLLOWS_FROM.equals(referenceType)) {
      refType = ReferenceType.FOLLOWS_FROM;
    } else {
      // Discard references with unrecognized type.
      return this;
    }

    // Optimization for 99% situations, when there is only one parent.
    if (allParents.size() == 0) {
      allParents =
          Collections.singletonList(
              SpanParentInfo.create(
                  contextShim.getSpanContext(), contextShim.getBaggage(), refType));
    } else {
      if (allParents.size() == 1) {
        allParents = new ArrayList<>(allParents);
      }
      allParents.add(
          SpanParentInfo.create(contextShim.getSpanContext(), contextShim.getBaggage(), refType));
    }

    return this;
  }

  @Override
  public SpanBuilder ignoreActiveSpan() {
    ignoreActiveSpan = true;
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, String value) {
    if (Tags.ERROR.getKey().equals(key)) {
      error = Boolean.parseBoolean(value);
    } else {
      this.spanBuilderAttributeKeys.add(stringKey(key));
      this.spanBuilderAttributeValues.add(value);
    }

    return this;
  }

  @Override
  public SpanBuilder withTag(String key, boolean value) {
    if (Tags.ERROR.getKey().equals(key)) {
      error = value;
    } else {
      this.spanBuilderAttributeKeys.add(booleanKey(key));
      this.spanBuilderAttributeValues.add(value);
    }
    return this;
  }

  @Override
  public SpanBuilder withTag(String key, Number value) {
    if (value == null) {
      return this;
    }

    if (value instanceof Integer
        || value instanceof Long
        || value instanceof Short
        || value instanceof Byte) {
      this.spanBuilderAttributeKeys.add(longKey(key));
      this.spanBuilderAttributeValues.add(value.longValue());
    } else if (value instanceof Float || value instanceof Double) {
      this.spanBuilderAttributeKeys.add(doubleKey(key));
      this.spanBuilderAttributeValues.add(value.doubleValue());
    } else {
      this.spanBuilderAttributeKeys.add(stringKey(key));
      this.spanBuilderAttributeValues.add(value.toString());
    }

    return this;
  }

  @Override
  public <T> SpanBuilder withTag(Tag<T> tag, T value) {
    if (tag == null) {
      return this;
    }
    if (value instanceof String) {
      this.withTag(tag.getKey(), (String) value);
    } else if (value instanceof Boolean) {
      this.withTag(tag.getKey(), (Boolean) value);
    } else if (value instanceof Number) {
      this.withTag(tag.getKey(), (Number) value);
    } else {
      this.withTag(tag.getKey(), value.toString());
    }

    return this;
  }

  @Override
  public SpanBuilder withStartTimestamp(long microseconds) {
    this.startTimestampMicros = microseconds;
    return this;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Span start() {
    Baggage baggage;
    io.opentelemetry.api.trace.SpanBuilder builder = tracer.spanBuilder(spanName);
    io.opentelemetry.api.trace.SpanContext mainParent = getMainParent(allParents);

    if (ignoreActiveSpan && mainParent == null) {
      builder.setNoParent();
      baggage = Baggage.empty();
    } else if (mainParent != null) {
      builder.setParent(Context.root().with(io.opentelemetry.api.trace.Span.wrap(mainParent)));
      baggage = getAllBaggage(allParents);
    } else {
      // No explicit parent Span, but extracted baggage may be available.
      baggage = Baggage.current();
    }

    // *All* parents are processed as Links, in order to keep the reference type value.
    for (SpanParentInfo parentInfo : allParents) {
      builder.addLink(
          parentInfo.getSpanContext(),
          parentInfo.getRefType() == ReferenceType.CHILD_OF ? CHILD_OF_ATTR : FOLLOWS_FROM_ATTR);
    }

    if (startTimestampMicros > 0) {
      builder.setStartTimestamp(startTimestampMicros, TimeUnit.MICROSECONDS);
    }

    // Attributes passed to the OT SpanBuilder MUST
    // be set before the OTel Span is created,
    // so those attributes are available to the Sampling API.
    for (int i = 0; i < this.spanBuilderAttributeKeys.size(); i++) {
      AttributeKey key = this.spanBuilderAttributeKeys.get(i);
      Object value = this.spanBuilderAttributeValues.get(i);
      builder.setAttribute(key, value);
    }

    io.opentelemetry.api.trace.Span span = builder.startSpan();
    if (error != null) {
      span.setStatus(error ? StatusCode.ERROR : StatusCode.OK);
    }

    return new SpanShim(span, baggage);
  }

  // The first SpanContext with Child Of type in the entire list is used as parent,
  // else the first SpanContext is used as parent.
  @Nullable
  private static io.opentelemetry.api.trace.SpanContext getMainParent(
      List<SpanParentInfo> parents) {
    if (parents.size() == 0) {
      return null;
    }

    SpanParentInfo mainParent = parents.get(0);
    for (SpanParentInfo parentInfo : parents) {
      if (parentInfo.getRefType() == ReferenceType.CHILD_OF) {
        mainParent = parentInfo;
        break;
      }
    }

    return mainParent.getSpanContext();
  }

  private static Baggage getAllBaggage(List<SpanParentInfo> parents) {
    if (parents.size() == 0) {
      return Baggage.empty();
    }

    if (parents.size() == 1) {
      return parents.get(0).getBaggage();
    }

    BaggageBuilder builder = Baggage.builder();
    for (SpanParentInfo parent : parents) {
      parent.getBaggage().forEach((key, entry) -> builder.put(key, entry.getValue()));
    }

    return builder.build();
  }

  @AutoValue
  @Immutable
  abstract static class SpanParentInfo {
    private static SpanParentInfo create(
        io.opentelemetry.api.trace.SpanContext spanContext,
        Baggage baggage,
        ReferenceType refType) {
      return new AutoValue_SpanBuilderShim_SpanParentInfo(spanContext, baggage, refType);
    }

    abstract io.opentelemetry.api.trace.SpanContext getSpanContext();

    abstract Baggage getBaggage();

    abstract ReferenceType getRefType();
  }

  enum ReferenceType {
    CHILD_OF,
    FOLLOWS_FROM
  }
}
