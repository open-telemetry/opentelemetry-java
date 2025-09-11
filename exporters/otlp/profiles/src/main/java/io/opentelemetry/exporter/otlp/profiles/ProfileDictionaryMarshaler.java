/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.ProfilesDictionary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ProfileDictionaryMarshaler extends MarshalerWithSize {

  private final MappingMarshaler[] mappingTableMarshalers;
  private final LocationMarshaler[] locationTableMarshalers;
  private final FunctionMarshaler[] functionTableMarshalers;
  private final LinkMarshaler[] linkTableMarshalers;
  private final byte[][] stringTable;
  private final KeyValueAndUnitMarshaler[] attributeTableMarshalers;
  private final StackMarshaler[] stackTableMarshalers;

  static ProfileDictionaryMarshaler create(ProfileDictionaryData profileDictionaryData) {

    MappingMarshaler[] mappingMarshalers =
        MappingMarshaler.createRepeated(profileDictionaryData.getMappingTable());
    LocationMarshaler[] locationMarshalers =
        LocationMarshaler.createRepeated(profileDictionaryData.getLocationTable());
    FunctionMarshaler[] functionMarshalers =
        FunctionMarshaler.createRepeated(profileDictionaryData.getFunctionTable());
    LinkMarshaler[] linkMarshalers =
        LinkMarshaler.createRepeated(profileDictionaryData.getLinkTable());

    byte[][] convertedStrings = new byte[profileDictionaryData.getStringTable().size()][];
    for (int i = 0; i < profileDictionaryData.getStringTable().size(); i++) {
      convertedStrings[i] =
          profileDictionaryData.getStringTable().get(i).getBytes(StandardCharsets.UTF_8);
    }

    KeyValueAndUnitMarshaler[] attributeTableMarshalers =
        KeyValueAndUnitMarshaler.createRepeated(profileDictionaryData.getAttributeTable());
    StackMarshaler[] stackTableMarshalers =
        StackMarshaler.createRepeated(profileDictionaryData.getStackTable());

    return new ProfileDictionaryMarshaler(
        mappingMarshalers,
        locationMarshalers,
        functionMarshalers,
        linkMarshalers,
        convertedStrings,
        attributeTableMarshalers,
        stackTableMarshalers);
  }

  private ProfileDictionaryMarshaler(
      MappingMarshaler[] mappingTableMarshalers,
      LocationMarshaler[] locationTableMarshalers,
      FunctionMarshaler[] functionTableMarshalers,
      LinkMarshaler[] linkTableMarshalers,
      byte[][] stringTableUtf8,
      KeyValueAndUnitMarshaler[] attributeTableMarshalers,
      StackMarshaler[] stackTableMarshalers) {
    super(
        calculateSize(
            mappingTableMarshalers,
            locationTableMarshalers,
            functionTableMarshalers,
            linkTableMarshalers,
            stringTableUtf8,
            attributeTableMarshalers,
            stackTableMarshalers));
    this.mappingTableMarshalers = mappingTableMarshalers;
    this.locationTableMarshalers = locationTableMarshalers;
    this.functionTableMarshalers = functionTableMarshalers;
    this.linkTableMarshalers = linkTableMarshalers;
    this.stringTable = stringTableUtf8;
    this.attributeTableMarshalers = attributeTableMarshalers;
    this.stackTableMarshalers = stackTableMarshalers;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(ProfilesDictionary.MAPPING_TABLE, mappingTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.LOCATION_TABLE, locationTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.FUNCTION_TABLE, functionTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.LINK_TABLE, linkTableMarshalers);
    output.serializeRepeatedString(ProfilesDictionary.STRING_TABLE, stringTable);
    output.serializeRepeatedMessage(ProfilesDictionary.ATTRIBUTE_TABLE, attributeTableMarshalers);
    output.serializeRepeatedMessage(ProfilesDictionary.STACK_TABLE, stackTableMarshalers);
  }

  private static int calculateSize(
      MappingMarshaler[] mappingMarshalers,
      LocationMarshaler[] locationMarshalers,
      FunctionMarshaler[] functionMarshalers,
      LinkMarshaler[] linkMarshalers,
      byte[][] stringTable,
      KeyValueAndUnitMarshaler[] attributeTableMarshalers,
      StackMarshaler[] stackTableMarshalers) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.MAPPING_TABLE, mappingMarshalers);
    size +=
        MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.LOCATION_TABLE, locationMarshalers);
    size +=
        MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.FUNCTION_TABLE, functionMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.LINK_TABLE, linkMarshalers);
    size += MarshalerUtil.sizeRepeatedString(ProfilesDictionary.STRING_TABLE, stringTable);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ProfilesDictionary.ATTRIBUTE_TABLE, attributeTableMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(ProfilesDictionary.STACK_TABLE, stackTableMarshalers);

    return size;
  }
}
