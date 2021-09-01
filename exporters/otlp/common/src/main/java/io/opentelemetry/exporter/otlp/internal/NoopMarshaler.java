/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

enum NoopMarshaler implements Marshaler {
  INSTANCE;

  @Override
  public void writeTo(CodedOutputStream output) {}

  @Override
  public int getProtoSerializedSize() {
    return 0;
  }
}
