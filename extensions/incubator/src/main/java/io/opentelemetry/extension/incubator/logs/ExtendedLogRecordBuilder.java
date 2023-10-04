/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;

public interface ExtendedLogRecordBuilder extends LogRecordBuilder {

  LogRecordBuilder setBody(AnyValue<?> body);
}
