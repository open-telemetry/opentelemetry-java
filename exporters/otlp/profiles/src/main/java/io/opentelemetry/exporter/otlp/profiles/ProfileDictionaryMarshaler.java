/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.profiles.v1development.internal.ProfilesDictionary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ProfileDictionaryMarshaler extends MarshalerWithSize {

  private final MappingMarshaler[] mappingTableMarshalers;
  private final LocationMarshaler[] locationTableMarshalers;
  private final FunctionMarshaler[] functionTableMarshalers;
  private final KeyValueMarshaler[] attributeTableMarshalers;
  private final AttributeUnitMarshaler[] attributeUnitMarshalers;
  private final LinkMarshaler[] linkTableMarshalers;
  private final byte[][] stringTable;

  static ProfileDictionaryMarshaler create(ProfileDictionaryData profileDictionaryData) {

    MappingMarshaler[] mappingMarshalers =
        MappingMarshaler.createRepeated(profileDictionaryData.getMappingTable());
    LocationMarshaler[] locationMarshalers =
        LocationMarshaler.createRepeated(profileDictionaryData.getLocationTable());
    FunctionMarshaler[] functionMarshalers =
        FunctionMarshaler.createRepeated(profileDictionaryData.getFunctionTable());
    KeyValueMarshaler[] attributeTableMarshalers =
        KeyValueMarshaler.createRepeated(profileDictionaryData.getAttributeTable());
    AttributeUnitMarshaler[] attributeUnitsMarshalers =
        AttributeUnitMarshaler.createRepeated(profileDictionaryData.getAttributeUnits());
    LinkMarshaler[] linkMarshalers =
        LinkMarshaler.createRepeated(profileDictionaryData.getLinkTable());

    byte[][] convertedStrings = new byte[profileDictionaryData.getStringTable().size()][];
    for (int i = 0; i < profileDictionaryData.getStringTable().size(); i++) {
      convertedStrings[i] =
          profileDictionaryData.getStringTable().get(i).getBytes(StandardCharsets.UTF_8);
    }

    return new ProfileDictionaryMarshaler(
        mappingMarshalers,
        locationMarshalers,
        functionMarshalers,
        attributeTableMarshalers,
        attributeUnitsMarshalers,
        linkMarshalers,
        convertedStrings);
  }

  private ProfileDictionaryMarshaler(
      MappingMarshaler[] mappingTableMarshalers,
      LocationMarshaler[] locationTableMarshalers,
      FunctionMarshaler[] functionTableMarshalers,
      KeyValueMarshaler[] attributeTableMarshalers,
      AttributeUnitMarshaler[] attributeUnitMarshalers,
      LinkMarshaler[] linkTableMarshalers,
      byte[][] stringTableUtf8) {
    super(
        calculateSize(
            mappingTableMarshalers,
            locationTableMarshalers,
            functionTableMarshalers,
            attributeTableMarshalers,
            attributeUnitMarshalers,
            linkTableMarshalers,
            stringTableUtf8));
    this.mappingTableMarshalers = mappingTableMarshalers;
    this.locationTableMarshalers = locationTableMarshalers;
    this.functionTableMarshalers = functionTableMarshalers;
    this.attributeTableMarshalers = attributeTableMarshalers;
    this.attributeUnitMarshalers = attributeUnitMarshalers;
    this.linkTableMarshalers = linkTableMarshalers;
    this.stringTable = stringTableUtf8;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(ProfilesDictionary.MAPPING_TABLE, mappingTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.LOCATION_TABLE, locationTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.FUNCTION_TABLE, functionTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.ATTRIBUTE_TABLE, attributeTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.ATTRIBUTE_UNITS, attributeUnitMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.LINK_TABLE, linkTableMarshalers);
    output.serializeRepeatedString(ProfilesDictionary.STRING_TABLE, stringTable);
  }

  private static int calculateSize(
      MappingMarshaler[] mappingMarshalers,
      LocationMarshaler[] locationMarshalers,
      FunctionMarshaler[] functionMarshalers,
      KeyValueMarshaler[] attributeTableMarshalers,
      AttributeUnitMarshaler[] attributeUnitMarshalers,
      LinkMarshaler[] linkMarshalers,
      byte[][] stringTable) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.MAPPING_TABLE, mappingMarshalers);
    size +=
        MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.LOCATION_TABLE, locationMarshalers);
    size +=
        MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.FUNCTION_TABLE, functionMarshalers);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ProfilesDictionary.ATTRIBUTE_TABLE, attributeTableMarshalers);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ProfilesDictionary.ATTRIBUTE_UNITS, attributeUnitMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.LINK_TABLE, linkMarshalers);
    size += MarshalerUtil.sizeRepeatedString(ProfilesDictionary.STRING_TABLE, stringTable);

    return size;
  }
}
