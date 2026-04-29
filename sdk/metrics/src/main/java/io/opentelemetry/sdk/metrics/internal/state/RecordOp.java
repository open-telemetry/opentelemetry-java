/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

public interface RecordOp {

  /** Records a measurement. */
  void recordLong(long value);

  /** Records a measurement. */
  void recordDouble(double value);
}
