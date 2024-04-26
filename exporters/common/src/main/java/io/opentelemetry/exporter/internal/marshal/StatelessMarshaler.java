/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;

public interface StatelessMarshaler<T> {

  int getBinarySerializedSize(T value, MarshalerContext context);

  void writeTo(Serializer output, T value, MarshalerContext context) throws IOException;
}
