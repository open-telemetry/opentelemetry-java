/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributesBuilder;

@FunctionalInterface
public interface SpanExceptionRecorder {
  AttributesBuilder recordException(Throwable exception, AttributesBuilder attributesBuilder);
}
