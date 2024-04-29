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
public interface StatelessMarshaler<T> {

  /** Returns the number of bytes marshaling given value will write in proto binary format. */
  int getBinarySerializedSize(T value, MarshalerContext context);

  /** Marshal given value using the provided {@link Serializer}. */
  void writeTo(Serializer output, T value, MarshalerContext context) throws IOException;
}
