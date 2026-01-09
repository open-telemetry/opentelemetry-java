/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.Sample;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class SampleMarshaler extends MarshalerWithSize {

  private static final SampleMarshaler[] EMPTY_REPEATED = new SampleMarshaler[0];

  private final int stackIndex;
  private final List<Long> values;
  private final List<Integer> attributeIndices;
  private final int linkIndex;
  private final List<Long> timestamps;

  static SampleMarshaler create(SampleData sampleData) {

    return new SampleMarshaler(
        sampleData.getStackIndex(),
        sampleData.getValues(),
        sampleData.getAttributeIndices(),
        sampleData.getLinkIndex(),
        sampleData.getTimestamps());
  }

  static SampleMarshaler[] createRepeated(List<SampleData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    SampleMarshaler[] sampleMarshalers = new SampleMarshaler[items.size()];
    items.forEach(
        new Consumer<SampleData>() {
          int index = 0;

          @Override
          public void accept(SampleData sampleData) {
            sampleMarshalers[index++] = SampleMarshaler.create(sampleData);
          }
        });
    return sampleMarshalers;
  }

  private SampleMarshaler(
      int stackIndex,
      List<Long> values,
      List<Integer> attributeIndices,
      int linkIndex,
      List<Long> timestamps) {
    super(calculateSize(stackIndex, values, attributeIndices, linkIndex, timestamps));
    this.stackIndex = stackIndex;
    this.values = values;
    this.attributeIndices = attributeIndices;
    this.linkIndex = linkIndex;
    this.timestamps = timestamps;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32(Sample.STACK_INDEX, stackIndex);
    output.serializeRepeatedInt64(Sample.VALUES, values);
    output.serializeRepeatedInt32(Sample.ATTRIBUTE_INDICES, attributeIndices);
    output.serializeInt32(Sample.LINK_INDEX, linkIndex);
    output.serializeRepeatedFixed64(Sample.TIMESTAMPS_UNIX_NANO, timestamps);
  }

  private static int calculateSize(
      int stackIndex,
      List<Long> values,
      List<Integer> attributeIndices,
      int linkIndex,
      List<Long> timestamps) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt32(Sample.STACK_INDEX, stackIndex);
    size += MarshalerUtil.sizeRepeatedInt64(Sample.VALUES, values);
    size += MarshalerUtil.sizeRepeatedInt32(Sample.ATTRIBUTE_INDICES, attributeIndices);
    size += MarshalerUtil.sizeInt32(Sample.LINK_INDEX, linkIndex);
    size += MarshalerUtil.sizeRepeatedFixed64(Sample.TIMESTAMPS_UNIX_NANO, timestamps);
    return size;
  }
}
