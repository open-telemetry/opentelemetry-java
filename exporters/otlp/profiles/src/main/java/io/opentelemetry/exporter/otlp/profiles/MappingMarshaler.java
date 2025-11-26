/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.Mapping;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class MappingMarshaler extends MarshalerWithSize {

  private static final MappingMarshaler[] EMPTY_REPEATED = new MappingMarshaler[0];

  private final long memoryStart;
  private final long memoryLimit;
  private final long fileOffset;
  private final int filenameIndex;
  private final List<Integer> attributeIndices;

  static MappingMarshaler create(MappingData mappingData) {
    return new MappingMarshaler(
        mappingData.getMemoryStart(),
        mappingData.getMemoryLimit(),
        mappingData.getFileOffset(),
        mappingData.getFilenameStringIndex(),
        mappingData.getAttributeIndices());
  }

  static MappingMarshaler[] createRepeated(List<MappingData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    MappingMarshaler[] mappingMarshalers = new MappingMarshaler[items.size()];
    items.forEach(
        new Consumer<MappingData>() {
          int index = 0;

          @Override
          public void accept(MappingData mappingData) {
            mappingMarshalers[index++] = MappingMarshaler.create(mappingData);
          }
        });
    return mappingMarshalers;
  }

  private MappingMarshaler(
      long memoryStart,
      long memoryLimit,
      long fileOffset,
      int filenameIndex,
      List<Integer> attributeIndices) {
    super(calculateSize(memoryStart, memoryLimit, fileOffset, filenameIndex, attributeIndices));
    this.memoryStart = memoryStart;
    this.memoryLimit = memoryLimit;
    this.fileOffset = fileOffset;
    this.filenameIndex = filenameIndex;
    this.attributeIndices = attributeIndices;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeUInt64(Mapping.MEMORY_START, memoryStart);
    output.serializeUInt64(Mapping.MEMORY_LIMIT, memoryLimit);
    output.serializeUInt64(Mapping.FILE_OFFSET, fileOffset);
    output.serializeInt32(Mapping.FILENAME_STRINDEX, filenameIndex);
    output.serializeRepeatedInt32(Mapping.ATTRIBUTE_INDICES, attributeIndices);
  }

  private static int calculateSize(
      long memoryStart,
      long memoryLimit,
      long fileOffset,
      int filenameIndex,
      List<Integer> attributeIndices) {
    int size = 0;
    size += MarshalerUtil.sizeUInt64(Mapping.MEMORY_START, memoryStart);
    size += MarshalerUtil.sizeUInt64(Mapping.MEMORY_LIMIT, memoryLimit);
    size += MarshalerUtil.sizeUInt64(Mapping.FILE_OFFSET, fileOffset);
    size += MarshalerUtil.sizeInt32(Mapping.FILENAME_STRINDEX, filenameIndex);
    size += MarshalerUtil.sizeRepeatedInt32(Mapping.ATTRIBUTE_INDICES, attributeIndices);
    return size;
  }
}
