/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;

final class NoopProcessContextPublisher implements ProcessContextPublisher {

  @Override
  public void publish(ProcessContextData processContextData, Clock clock) {}

  @Override
  public void close() {}
}
