/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.Stack;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class StackMarshaler extends MarshalerWithSize {

  private static final StackMarshaler[] EMPTY_REPEATED = new StackMarshaler[0];

  private final List<Integer> locationIndices;

  static StackMarshaler create(StackData stackData) {
    return new StackMarshaler(stackData.getLocationIndices());
  }

  static StackMarshaler[] createRepeated(List<StackData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    StackMarshaler[] stackMarshalers = new StackMarshaler[items.size()];
    items.forEach(
        new Consumer<StackData>() {
          int index = 0;

          @Override
          public void accept(StackData stackData) {
            stackMarshalers[index++] = StackMarshaler.create(stackData);
          }
        });
    return stackMarshalers;
  }

  private StackMarshaler(List<Integer> locationIndices) {
    super(calculateSize(locationIndices));
    this.locationIndices = locationIndices;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedInt32(Stack.LOCATION_INDICES, locationIndices);
  }

  private static int calculateSize(List<Integer> locationIndices) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedInt32(Stack.LOCATION_INDICES, locationIndices);
    return size;
  }
}
