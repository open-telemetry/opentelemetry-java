/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

final class CustomSourceInfo implements SourceInfo {
  private final String sourcePath;
  private final int lineNumber;

  CustomSourceInfo(String sourcePath, int lineNumber) {
    this.sourcePath = sourcePath;
    this.lineNumber = lineNumber;
  }

  @Override
  public String shortDebugString() {
    return String.format("%s:%d", sourcePath, lineNumber);
  }

  @Override
  public String multiLineDebugString() {
    return String.format("\tat %s:%d", sourcePath, lineNumber);
  }
}
