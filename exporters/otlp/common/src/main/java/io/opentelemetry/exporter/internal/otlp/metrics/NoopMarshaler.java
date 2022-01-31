/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;

final class NoopMarshaler extends MarshalerWithSize {

  static final NoopMarshaler INSTANCE = new NoopMarshaler();

  private NoopMarshaler() {
    super(0);
  }

  @Override
  public void writeTo(Serializer output) {}
}
