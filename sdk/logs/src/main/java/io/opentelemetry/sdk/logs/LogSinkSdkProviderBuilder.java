/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

public final class LogSinkSdkProviderBuilder {
  LogSinkSdkProviderBuilder() {}

  public LogSinkSdkProvider build() {
    return new LogSinkSdkProvider();
  }
}
