/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

/**
 * An interface that can be used to record the (runtime) source of registered metrics in the sdk.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface SourceInfo {
  /**
   * Returns a debugging string to report where a given metric was registered.
   *
   * <p>Example: {@code MyFile.java:15}
   */
  String shortDebugString();

  /**
   * Returns a multi-line debugging string to report where a given metric was registered.
   *
   * <p>Example:
   *
   * <pre>
   *   at full.package.name.method MyFile.java:15
   *   at full.packae.name.otherMethod MyOtherFile.java:10
   * </pre>
   */
  String multiLineDebugString();

  /** Returns a source info that asks the user to register information. */
  static SourceInfo noSourceInfo() {
    return NoSourceInfo.INSTANCE;
  }

  /**
   * Constructs source information form the current stack.
   *
   * <p>This will attempt to ignore SDK classes.
   */
  static SourceInfo fromCurrentStack() {
    if (!DebugConfig.isMetricsDebugEnabled()) {
      return noSourceInfo();
    }
    return new StackTraceSourceInfo(Thread.currentThread().getStackTrace());
  }

  /**
   * Constructs a custom source information object that is meant to represent a non-code source of
   * metric configuration.
   *
   * @param sourcePath A URI, filename or other user-identifying feature of where the metric
   *     configuration came from.
   * @param lineNumber A line number in the source, denoting where the configuration resides.
   */
  static SourceInfo fromConfigFile(String sourcePath, int lineNumber) {
    return new CustomSourceInfo(sourcePath, lineNumber);
  }
}
