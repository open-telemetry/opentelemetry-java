/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;

/** Extended {@link LogRecordBuilder} with experimental APIs. */
public interface ExtendedLogRecordBuilder extends LogRecordBuilder {

  /** Set the body {@link AnyValue}. */
  LogRecordBuilder setBody(AnyValue<?> body);
}
