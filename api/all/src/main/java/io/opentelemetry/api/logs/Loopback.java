/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;

public final class Loopback {

  private Loopback() {}

  public static final ContextKey<Boolean> loopbackContextKey =
      ContextKey.named("otel.loopback");

  public static Context withLoopback(Context context) {
    return context.with(loopbackContextKey, true);
  }

  public static boolean isLoopback(Context context) {
    Boolean loopback = context.get(loopbackContextKey);
    return loopback != null && loopback;
  }
}
