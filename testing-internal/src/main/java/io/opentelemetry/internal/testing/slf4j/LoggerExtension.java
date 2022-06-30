/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.internal.testing.slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 extension to enable the {@link SuppressLogger} annotation which can suppress output of log
 * messages during test execution, i.e. when the log messages are expected.
 */
public final class LoggerExtension
    implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(LoggerExtension.class);

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    List<SuppressLogger> suppressLoggers = new ArrayList<>();
    suppressLoggers.addAll(
        Arrays.asList(context.getRequiredTestMethod().getAnnotationsByType(SuppressLogger.class)));
    suppressLoggers.addAll(
        Arrays.asList(context.getRequiredTestClass().getAnnotationsByType(SuppressLogger.class)));

    List<Logger> loggers = new ArrayList<>();
    for (SuppressLogger suppression : suppressLoggers) {
      if (!suppression.value().equals(Void.class)) {
        loggers.add(Logger.getLogger(suppression.value().getName()));
      }
      if (!suppression.loggerName().isEmpty()) {
        loggers.add(Logger.getLogger(suppression.loggerName()));
      }
    }

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
