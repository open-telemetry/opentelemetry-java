/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

final class NoopMarshaler extends MarshalerWithSize {

  static final NoopMarshaler INSTANCE = new NoopMarshaler();

  private NoopMarshaler() {
    super(0);
  }

  @Override
  public void writeTo(Serializer output) {}
}
