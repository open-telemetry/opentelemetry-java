/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;

final class NoopMarshaler extends MarshalerWithSize {

  static final NoopMarshaler INSTANCE = new NoopMarshaler();

  private NoopMarshaler() {
    super(0);
  }

  @Override
  public void writeTo(Serializer output) {}
}
