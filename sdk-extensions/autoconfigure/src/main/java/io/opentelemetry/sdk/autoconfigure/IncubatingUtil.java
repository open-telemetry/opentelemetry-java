/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;

/**
 * Utilities for interacting with {@code io.opentelemetry:opentelemetry-api-incubator}, which is not
 * guaranteed to be present on the classpath. For all methods, callers MUST first separately
 * reflectively confirm that the incubator is available on the classpath.
 */
final class IncubatingUtil {

  static void setGlobalEventLoggerProvider(SdkLoggerProvider sdkLoggerProvider) {
    GlobalEventLoggerProvider.set(SdkEventLoggerProvider.create(sdkLoggerProvider));
  }
}
