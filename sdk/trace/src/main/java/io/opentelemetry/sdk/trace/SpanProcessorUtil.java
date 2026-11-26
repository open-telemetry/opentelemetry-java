/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.internal.ThrowableUtil;
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Invokes span processor callbacks and logs nonfatal failures. */
final class SpanProcessorUtil {
  private static final Logger logger = Logger.getLogger(SpanProcessorUtil.class.getName());

  static void onStart(SpanProcessor processor, Context parentContext, ReadWriteSpan span) {
    try {
      processor.onStart(parentContext, span);
    } catch (Throwable t) {
      ThrowableUtil.propagateIfFatal(t);
      logger.log(Level.WARNING, "SpanProcessor.onStart failed", t);
    }
  }

  static void onEnding(ExtendedSpanProcessor processor, ReadWriteSpan span) {
    try {
      processor.onEnding(span);
    } catch (Throwable t) {
      ThrowableUtil.propagateIfFatal(t);
      logger.log(Level.WARNING, "SpanProcessor.onEnding failed", t);
    }
  }

  static void onEnd(SpanProcessor processor, ReadableSpan span) {
    try {
      processor.onEnd(span);
    } catch (Throwable t) {
      ThrowableUtil.propagateIfFatal(t);
      logger.log(Level.WARNING, "SpanProcessor.onEnd failed", t);
    }
  }

  private SpanProcessorUtil() {}
}
