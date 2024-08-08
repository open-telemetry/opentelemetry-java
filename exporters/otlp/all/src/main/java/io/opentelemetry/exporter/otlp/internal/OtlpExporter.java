/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;

public interface OtlpExporter<T extends Marshaler> {
  CompletableResultCode export(T exportRequest, int numItems);

  CompletableResultCode shutdown();
}
