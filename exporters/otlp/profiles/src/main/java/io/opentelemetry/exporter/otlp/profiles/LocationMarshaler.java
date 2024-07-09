/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1experimental.internal.Location;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class LocationMarshaler extends MarshalerWithSize {

  private static final LocationMarshaler[] EMPTY_REPEATED = new LocationMarshaler[0];

  private final long mappingIndex;
  private final long address;
  private final LineMarshaler[] lineMarshalers;
  private final boolean isFolded;
  private final int typeIndex;
  private final List<Long> attributes;

  static LocationMarshaler create(LocationData locationData) {
    LocationMarshaler locationMarshaler =
        new LocationMarshaler(
            locationData.getMappingIndex(),
            locationData.getAddress(),
            LineMarshaler.createRepeated(locationData.getLines()),
            locationData.isFolded(),
            locationData.getTypeIndex(),
            locationData.getAttributes());
    return locationMarshaler;
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
      long mappingIndex,
      long address,
      LineMarshaler[] lineMarshalers,
      boolean isFolded,
      int typeIndex,
      List<Long> attributes) {
    super(calculateSize(mappingIndex, address, lineMarshalers, isFolded, typeIndex, attributes));
    this.mappingIndex = mappingIndex;
    this.address = address;
    this.lineMarshalers = lineMarshalers;
    this.isFolded = isFolded;
    this.typeIndex = typeIndex;
    this.attributes = attributes;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeUInt64(Location.MAPPING_INDEX, mappingIndex);
    output.serializeUInt64(Location.ADDRESS, address);
    output.serializeRepeatedMessage(Location.LINE, lineMarshalers);
    output.serializeBool(Location.IS_FOLDED, isFolded);
    output.serializeUInt32(Location.TYPE_INDEX, typeIndex);
    output.serializeRepeatedUInt64(Location.ATTRIBUTES, attributes);
  }

  private static int calculateSize(
      long mappingIndex,
      long address,
      LineMarshaler[] lineMarshalers,
      boolean isFolded,
      int typeIndex,
      List<Long> attributes) {
    int size = 0;
    size += MarshalerUtil.sizeUInt64(Location.MAPPING_INDEX, mappingIndex);
    size += MarshalerUtil.sizeUInt64(Location.ADDRESS, address);
    size += MarshalerUtil.sizeRepeatedMessage(Location.LINE, lineMarshalers);
    size += MarshalerUtil.sizeBool(Location.IS_FOLDED, isFolded);
    size += MarshalerUtil.sizeUInt32(Location.TYPE_INDEX, typeIndex);
    size += MarshalerUtil.sizeRepeatedUInt64(Location.ATTRIBUTES, attributes);
    return size;
  }
}
