/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;
import java.util.Objects;

final class NoopProcessContextPublisher implements ProcessContextPublisher {

  @Override
  public void publish(ProcessContextData processContextData, Clock clock) {
    Objects.requireNonNull(processContextData, "processContextData");
    Objects.requireNonNull(clock, "clock");
  }

  @Override
  public void close() {}
}
