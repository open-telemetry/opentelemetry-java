/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.logs.data.LogRecordData;
import org.assertj.core.api.Assertions;

/** Test assertions for data heading to exporters within the Metrics SDK. */
public final class LogAssertions extends Assertions {

  /** Returns an assertion for {@link LogRecordData}. */
  public static LogRecordDataAssert assertThat(LogRecordData log) {
    return new LogRecordDataAssert(log);
  }

  private LogAssertions() {}
}
