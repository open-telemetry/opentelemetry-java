/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import java.io.PrintStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * An alternative to exception stacktrace renderer that replicates the behavior of {@link
 * Throwable#printStackTrace(PrintStream)}, but which is aware of a maximum stacktrace length limit,
 * and exits early when the length limit has been exceeded to avoid unnecessary computation.
 *
 * <p>Instances should only be used once.
 */
class StackTraceRenderer {

  private static final String CAUSED_BY = "Caused by: ";
  private static final String SUPPRESSED = "Suppressed: ";

  private final Throwable throwable;
  private final int lengthLimit;
  private final StringBuilder builder = new StringBuilder();

  StackTraceRenderer(Throwable throwable, int lengthLimit) {
    this.throwable = throwable;
    this.lengthLimit = lengthLimit;
  }

  String render() {
    if (builder.length() == 0) {
      appendStackTrace();
    }

    return builder.substring(0, Math.min(builder.length(), lengthLimit));
  }

  private void appendStackTrace() {
    builder.append(throwable).append(System.lineSeparator());
    if (isOverLimit()) {
      return;
    }

    StackTraceElement[] stackTraceElements = throwable.getStackTrace();
    for (StackTraceElement stackTraceElement : stackTraceElements) {
      builder.append("\tat ").append(stackTraceElement).append(System.lineSeparator());
      if (isOverLimit()) {
        return;
      }
    }

    Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    seen.add(throwable);

    for (Throwable suppressed : throwable.getSuppressed()) {
      appendInnerStacktrace(stackTraceElements, suppressed, "\t", SUPPRESSED, seen);
    }

    Throwable cause = throwable.getCause();
    if (cause != null) {
      appendInnerStacktrace(stackTraceElements, cause, "", CAUSED_BY, seen);
    }
  }

  /**
   * Append the {@code innerThrowable} to the {@link #builder}, returning {@code true} if the
   * builder now exceeds the length limit.
   */
  private boolean appendInnerStacktrace(
      StackTraceElement[] parentElements,
      Throwable innerThrowable,
      String prefix,
      String caption,
      Set<Throwable> seen) {
    if (seen.contains(innerThrowable)) {
      builder
          .append(prefix)
          .append(caption)
          .append("[CIRCULAR REFERENCE: ")
          .append(innerThrowable)
          .append("]")
          .append(System.lineSeparator());
      return true;
    }
    seen.add(innerThrowable);

    // Iterating back to front, compute the lastSharedFrameIndex, which tracks the point at which
    // this exception's stacktrace elements start repeating the parent's elements
    StackTraceElement[] currentElements = innerThrowable.getStackTrace();
    int parentIndex = parentElements.length - 1;
    int lastSharedFrameIndex = currentElements.length - 1;
    while (true) {
      if (parentIndex < 0 || lastSharedFrameIndex < 0) {
        break;
      }
      if (!parentElements[parentIndex].equals(currentElements[lastSharedFrameIndex])) {
        break;
      }
      parentIndex--;
      lastSharedFrameIndex--;
    }

    builder.append(prefix).append(caption).append(innerThrowable).append(System.lineSeparator());
    if (isOverLimit()) {
      return true;
    }

    for (int i = 0; i <= lastSharedFrameIndex; i++) {
      StackTraceElement stackTraceElement = currentElements[i];
      builder
          .append(prefix)
          .append("\tat ")
          .append(stackTraceElement)
          .append(System.lineSeparator());
      if (isOverLimit()) {
        return true;
      }
    }

    int duplicateFrames = currentElements.length - 1 - lastSharedFrameIndex;
    if (duplicateFrames != 0) {
      builder
          .append(prefix)
          .append("\t... ")
          .append(duplicateFrames)
          .append(" more")
          .append(System.lineSeparator());
      if (isOverLimit()) {
        return true;
      }
    }

    for (Throwable suppressed : innerThrowable.getSuppressed()) {
      if (appendInnerStacktrace(currentElements, suppressed, prefix + "\t", SUPPRESSED, seen)) {
        return true;
      }
    }

    Throwable cause = innerThrowable.getCause();
    if (cause != null) {
      return appendInnerStacktrace(currentElements, cause, prefix, CAUSED_BY, seen);
    }

    return false;
  }

  private boolean isOverLimit() {
    return builder.length() >= lengthLimit;
  }
}
