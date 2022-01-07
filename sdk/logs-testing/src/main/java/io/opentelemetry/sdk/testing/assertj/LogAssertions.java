/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.logs.data.LogData;
import org.assertj.core.api.Assertions;

/** Test assertions for data heading to exporters within the Metrics SDK. */
public final class LogAssertions extends Assertions {

  /** Returns an assertion for {@link io.opentelemetry.sdk.logs.data.LogData}. */
  public static LogDataAssert assertThat(LogData log) {
    return new LogDataAssert(log);
  }

  private LogAssertions() {}
}
