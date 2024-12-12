/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;

final class IncubatingUtil {

  private IncubatingUtil() {}

  static void setGlobalEventLoggerProvider(SdkLoggerProvider sdkLoggerProvider) {
    GlobalEventLoggerProvider.set(SdkEventLoggerProvider.create(sdkLoggerProvider));
  }
}
