/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;

/**
 * Marshaler from an SDK structure to protobuf wire format. It is intended that the instances of
 * this interface don't keep marshaling state and can be singletons. Any state needed for marshaling
 * should be stored in {@link MarshalerContext}. Marshaler should be used so that first {@link
 * #getBinarySerializedSize} is called and after that {@link #writeTo} is called. Calling {@link
 * #getBinarySerializedSize} may add values to {@link MarshalerContext} that are later used in
 * {@link #writeTo}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface StatelessMarshaler2<K, V> {

  /** Returns the number of bytes this Marshaler will write. */
  int getBinarySerializedSize(K key, V value, MarshalerContext context);

  /** Marshal given key and value using the provided {@link Serializer}. */
  void writeTo(Serializer output, K key, V value, MarshalerContext context) throws IOException;
}
