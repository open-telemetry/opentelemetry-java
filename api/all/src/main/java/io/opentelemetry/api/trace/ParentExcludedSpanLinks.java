/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.context.ImplicitContextKeyed;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ParentExcludedSpanLinks implements SpanLinks {

  static SpanLinks create(SpanContext spanContext) {
    return new ParentExcludedSpanLinks(spanContext);
  }

  private final SpanContext parentSpanContext;
  private final Set<SpanContext> spanLinks;

  private ParentExcludedSpanLinks(SpanContext parentSpanContext) {
    this.parentSpanContext = parentSpanContext;
    spanLinks = null;
  }

  private ParentExcludedSpanLinks(SpanContext parentSpanContext, Set<SpanContext> spanLinks) {
    this.parentSpanContext = parentSpanContext;
    this.spanLinks = spanLinks;
  }

  @Override
  public ImplicitContextKeyed with(SpanContext spanContext) {
    if (parentSpanContext.equals(spanContext)) {
      return this;
    }
    if (spanLinks == null) {
      return new ParentExcludedSpanLinks(
          parentSpanContext, new LinkedHashSet<>(Collections.singleton(spanContext)));
    }
    LinkedHashSet<SpanContext> links = new LinkedHashSet<>(spanLinks);
    links.add(spanContext);
    return new ParentExcludedSpanLinks(parentSpanContext, links);
  }

  @Override
  public void consume(Consumer<SpanContext> consumer) {
    if (spanLinks != null) {
      spanLinks.forEach(consumer);
    }
  }

  @Override
  public String toString() {
    if (spanLinks == null) {
      return "ParentExcludedSpanLinks{parent=" + parentSpanContext + '}';
    } else {
      return "ParentExcludedSpanLinks{parent="
          + parentSpanContext
          + ", spanLinks="
          + Arrays.toString(spanLinks.toArray())
          + '}';
    }
  }
}
