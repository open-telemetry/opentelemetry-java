/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

/** A Callable returning a primitive long value. */
@FunctionalInterface
interface LongCallable {
  /** Returns the value. */
  long get();
}
