/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.Location;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class LocationMarshaler extends MarshalerWithSize {

  private static final LocationMarshaler[] EMPTY_REPEATED = new LocationMarshaler[0];

  @Nullable private final Integer mappingIndex;
  private final long address;
  private final LineMarshaler[] lineMarshalers;
  private final boolean isFolded;
  private final List<Integer> attributeIndices;

  static LocationMarshaler create(LocationData locationData) {
    return new LocationMarshaler(
        locationData.getMappingIndex(),
        locationData.getAddress(),
        LineMarshaler.createRepeated(locationData.getLines()),
        locationData.isFolded(),
        locationData.getAttributeIndices());
  }

  static LocationMarshaler[] createRepeated(List<LocationData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    LocationMarshaler[] locationMarshalers = new LocationMarshaler[items.size()];
    items.forEach(
        item ->
            new Consumer<LocationData>() {
              int index = 0;

              @Override
              public void accept(LocationData locationData) {
                locationMarshalers[index++] = LocationMarshaler.create(locationData);
              }
            });
    return locationMarshalers;
  }

  private LocationMarshaler(
      @Nullable Integer mappingIndex,
      long address,
      LineMarshaler[] lineMarshalers,
      boolean isFolded,
      List<Integer> attributeIndices) {
    super(calculateSize(mappingIndex, address, lineMarshalers, isFolded, attributeIndices));
    this.mappingIndex = mappingIndex;
    this.address = address;
    this.lineMarshalers = lineMarshalers;
    this.isFolded = isFolded;
    this.attributeIndices = attributeIndices;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32Optional(Location.MAPPING_INDEX, mappingIndex);
    output.serializeUInt64(Location.ADDRESS, address);
    output.serializeRepeatedMessage(Location.LINE, lineMarshalers);
    output.serializeBool(Location.IS_FOLDED, isFolded);
    output.serializeRepeatedInt32(Location.ATTRIBUTE_INDICES, attributeIndices);
  }

  private static int calculateSize(
      @Nullable Integer mappingIndex,
      long address,
      LineMarshaler[] lineMarshalers,
      boolean isFolded,
      List<Integer> attributeIndices) {
    int size = 0;
    size += MarshalerUtil.sizeInt32Optional(Location.MAPPING_INDEX, mappingIndex);
    size += MarshalerUtil.sizeUInt64(Location.ADDRESS, address);
    size += MarshalerUtil.sizeRepeatedMessage(Location.LINE, lineMarshalers);
    size += MarshalerUtil.sizeBool(Location.IS_FOLDED, isFolded);
    size += MarshalerUtil.sizeRepeatedInt32(Location.ATTRIBUTE_INDICES, attributeIndices);
    return size;
  }
}
