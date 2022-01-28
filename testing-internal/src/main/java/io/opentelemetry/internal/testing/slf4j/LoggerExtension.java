/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.internal.testing.slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class LoggerExtension
    implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(LoggerExtension.class);

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    List<Logger> loggers =
        Stream.concat(
                Arrays.stream(
                    context.getRequiredTestMethod().getAnnotationsByType(SuppressLogger.class)),
                Arrays.stream(
                    context.getRequiredTestClass().getAnnotationsByType(SuppressLogger.class)))
            .map(suppression -> Logger.getLogger(suppression.value().getName()))
            .collect(Collectors.toList());
    if (loggers.isEmpty()) {
      return;
    }

    loggers.forEach(logger -> logger.setUseParentHandlers(false));
    context
        .getStore(NAMESPACE)
        .put(
            LoggerExtension.class,
            (Runnable) () -> loggers.forEach(logger -> logger.setUseParentHandlers(true)));
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    Runnable restore = context.getStore(NAMESPACE).get(LoggerExtension.class, Runnable.class);
    if (restore != null) {
      restore.run();
    }
  }
}
