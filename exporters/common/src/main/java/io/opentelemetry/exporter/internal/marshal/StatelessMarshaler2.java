/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;

public interface StatelessMarshaler2<K, V> {

  int getBinarySerializedSize(K key, V value, MarshalerContext context);

  void writeTo(Serializer output, K key, V value, MarshalerContext context) throws IOException;
}
