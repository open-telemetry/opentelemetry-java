/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import io.opentelemetry.sdk.common.internal.ThrowableUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Invokes log record processor callbacks and logs nonfatal failures. */
final class LogRecordProcessorUtil {
  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(LogRecordProcessorUtil.class.getName()));

  static void onEmit(LogRecordProcessor processor, Context context, ReadWriteLogRecord logRecord) {
    try {
      processor.onEmit(context, logRecord);
    } catch (Throwable t) {
      ThrowableUtil.propagateIfFatal(t);
      logger.log(Level.WARNING, "LogRecordProcessor.onEmit failed", t);
    }
  }

  private LogRecordProcessorUtil() {}
}
