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
import javax.annotation.Nullable;

final class SampleMarshaler extends MarshalerWithSize {

  private static final SampleMarshaler[] EMPTY_REPEATED = new SampleMarshaler[0];

  private final int locationsStartIndex;
  private final int locationsLength;
  private final List<Long> values;
  private final List<Integer> attributeIndices;
  @Nullable private final Integer linkIndex;
  private final List<Long> timestamps;

  static SampleMarshaler create(SampleData sampleData) {

    return new SampleMarshaler(
        sampleData.getLocationsStartIndex(),
        sampleData.getLocationsLength(),
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
      int locationsStartIndex,
      int locationsLength,
      List<Long> values,
      List<Integer> attributeIndices,
      @Nullable Integer linkIndex,
      List<Long> timestamps) {
    super(
        calculateSize(
            locationsStartIndex, locationsLength, values, attributeIndices, linkIndex, timestamps));
    this.locationsStartIndex = locationsStartIndex;
    this.locationsLength = locationsLength;
    this.values = values;
    this.attributeIndices = attributeIndices;
    this.linkIndex = linkIndex;
    this.timestamps = timestamps;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32(Sample.LOCATIONS_START_INDEX, locationsStartIndex);
    output.serializeInt32(Sample.LOCATIONS_LENGTH, locationsLength);
    output.serializeRepeatedInt64(Sample.VALUE, values);
    output.serializeRepeatedInt32(Sample.ATTRIBUTE_INDICES, attributeIndices);
    output.serializeInt32Optional(Sample.LINK_INDEX, linkIndex);
    output.serializeRepeatedUInt64(Sample.TIMESTAMPS_UNIX_NANO, timestamps);
  }

  private static int calculateSize(
      int locationsStartIndex,
      int locationsLength,
      List<Long> values,
      List<Integer> attributeIndices,
      @Nullable Integer linkIndex,
      List<Long> timestamps) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt32(Sample.LOCATIONS_START_INDEX, locationsStartIndex);
    size += MarshalerUtil.sizeInt32(Sample.LOCATIONS_LENGTH, locationsLength);
    size += MarshalerUtil.sizeRepeatedInt64(Sample.VALUE, values);
    size += MarshalerUtil.sizeRepeatedInt32(Sample.ATTRIBUTE_INDICES, attributeIndices);
    size += MarshalerUtil.sizeInt32Optional(Sample.LINK_INDEX, linkIndex);
    size += MarshalerUtil.sizeRepeatedUInt64(Sample.TIMESTAMPS_UNIX_NANO, timestamps);
    return size;
  }
}
