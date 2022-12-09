/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.noop.NoopSpan;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

class ShimUtil {
  private static final Logger logger = Logger.getLogger(ShimUtil.class.getName());

  private ShimUtil() {}

  @Nullable
  static SpanContextShim getContextShim(@Nullable SpanContext context) {
    if (context == null) {
      return null;
    }

    if (!(context instanceof SpanContextShim)) {
      logger.log(
          Level.INFO,
          "Expected to have an OpenTelemetry SpanContext but got {0}",
          className(context));
      return null;
    }

    return (SpanContextShim) context;
  }

  @Nullable
  static SpanShim getSpanShim(@Nullable Span span) {
    if (span == null || span instanceof NoopSpan) {
      return null;
    }

    if (!(span instanceof SpanShim)) {
      if (span instanceof Supplier<?>) {
        // allow libraries to implement a delegate span,
        // such as https://github.com/zalando/opentracing-toolbox/tree/main/opentracing-proxy
        Object wrapped = ((Supplier<?>) span).get();
        if (wrapped instanceof Span) {
          return getSpanShim((Span) wrapped);
        } else {
          logger.log(Level.INFO, "Span wrapper didn't return a span: {0}", className(wrapped));
          return null;
        }
      }

      logger.log(Level.INFO, "Expected to have an OpenTelemetry Span but got {0}", className(span));
      return null;
    }

    return (SpanShim) span;
  }

  private static String className(@Nullable Object o) {
    return o == null ? "null" : o.getClass().getName();
  }
}
