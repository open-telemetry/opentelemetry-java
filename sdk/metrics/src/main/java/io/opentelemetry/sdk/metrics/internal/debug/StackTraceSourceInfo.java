/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

/** Diagnostic information derived from stack traces. */
final class StackTraceSourceInfo implements SourceInfo {

  private final StackTraceElement[] stackTraceElements;

  StackTraceSourceInfo(StackTraceElement[] stackTraceElements) {
    this.stackTraceElements = stackTraceElements;
  }

  @Override
  public String shortDebugString() {
    if (stackTraceElements.length > 0) {
      for (StackTraceElement e : stackTraceElements) {
        if (!isOTelSdkStackTrace(e)) {
          return String.format("%s:%d", e.getFileName(), e.getLineNumber());
        }
      }
    }
    return "unknown source";
  }

  @Override
  public String multiLineDebugString() {
    if (stackTraceElements.length > 0) {
      // TODO - Limit trace length
      StringBuffer result = new StringBuffer("");
      for (StackTraceElement e : stackTraceElements) {
        if (!isOTelSdkStackTrace(e)) {
          result.append("\tat ").append(e).append("\n");
        }
      }
      return result.toString();
    }
    return "\tat unknown source";
  }

  private static boolean isOTelSdkStackTrace(StackTraceElement e) {
    return (e.getClassName() != null)
        && e.getClassName().startsWith("io.opentelemetry.sdk.metrics");
  }
}
