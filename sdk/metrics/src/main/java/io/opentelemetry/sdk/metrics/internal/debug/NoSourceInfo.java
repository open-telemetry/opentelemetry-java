/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

enum NoSourceInfo implements SourceInfo {
  INSTANCE;

  @Override
  public String shortDebugString() {
    return "unknown source";
  }

  @Override
  public String multiLineDebugString() {
    return "\tat unknown source"
        + "\n\tTo enable debugging, please ensure OTEL_METRICS_DBEUGGING=true in the environment.";
  }
}
