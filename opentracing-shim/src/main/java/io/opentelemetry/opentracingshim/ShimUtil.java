/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import java.util.function.Supplier;
import javax.annotation.Nullable;

class ShimUtil {

  private ShimUtil() {}

  static SpanContextShim getContextShim(SpanContext context) {
    if (!(context instanceof SpanContextShim)) {
      throw new IllegalArgumentException(
          "context is not a valid SpanContextShim object: " + className(context));
    }

    return (SpanContextShim) context;
  }

  static SpanShim getSpanShim(Span span) {
    if (!(span instanceof SpanShim)) {
      if (span instanceof Supplier<?>) {
        // allow libraries to implement a delegate span,
        // such as https://github.com/zalando/opentracing-toolbox/tree/main/opentracing-proxy
        Object wrapped = ((Supplier<?>) span).get();
        if (wrapped instanceof Span) {
          return getSpanShim((Span) wrapped);
        } else {
          throw new IllegalArgumentException(
              "span wrapper didn't return a span: " + className(wrapped));
        }
      }
      throw new IllegalArgumentException("span is not a valid SpanShim object: " + className(span));
    }

    return (SpanShim) span;
  }

  private static String className(@Nullable Object o) {
    return o == null ? "null" : o.getClass().getName();
  }
}
