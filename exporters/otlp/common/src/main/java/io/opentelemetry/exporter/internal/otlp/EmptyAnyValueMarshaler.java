/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;

/**
 * A Marshaler of empty {@link io.opentelemetry.proto.common.v1.internal.AnyValue}. Represents an
 * AnyValue with no field set.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class EmptyAnyValueMarshaler extends MarshalerWithSize {

  static final EmptyAnyValueMarshaler INSTANCE = new EmptyAnyValueMarshaler();

  private EmptyAnyValueMarshaler() {
    super(0);
  }

  @Override
  public void writeTo(Serializer output) {
    // no field to write
  }
}
