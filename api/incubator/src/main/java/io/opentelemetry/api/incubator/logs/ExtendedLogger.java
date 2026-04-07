/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.logs.Logger;

/** Extended {@link Logger} with experimental APIs. */
public interface ExtendedLogger extends Logger {

  @Override
  ExtendedLogRecordBuilder logRecordBuilder();
}
