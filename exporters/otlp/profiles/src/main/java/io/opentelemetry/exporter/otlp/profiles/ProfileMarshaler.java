/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.profiles.v1development.internal.Profile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class ProfileMarshaler extends MarshalerWithSize {

  private final ValueTypeMarshaler[] sampleTypeMarshalers;
  private final SampleMarshaler[] sampleMarshalers;
  private final MappingMarshaler[] mappingTableMarshalers;
  private final LocationMarshaler[] locationTableMarshalers;
  private final List<Integer> locationIndices;
  private final FunctionMarshaler[] functionTableMarshalers;
  private final KeyValueMarshaler[] attributeTableMarshalers;
  private final AttributeUnitMarshaler[] attributeUnitMarshalers;
  private final LinkMarshaler[] linkTableMarshalers;
  private final byte[][] stringTable;
  private final long timeNanos;
  private final long durationNanos;
  private final ValueTypeMarshaler periodTypeMarshaler;
  private final long period;
  private final List<Integer> comment;
  private final int defaultSampleType;
  private final byte[] profileId;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final byte[] originalPayloadFormatUtf8;
  private final ByteBuffer originalPayload;

  static ProfileMarshaler create(ProfileData profileData) {

    ValueTypeMarshaler[] sampleTypeMarshalers =
        ValueTypeMarshaler.createRepeated(profileData.getSampleTypes());
    SampleMarshaler[] sampleMarshalers = SampleMarshaler.createRepeated(profileData.getSamples());
    MappingMarshaler[] mappingMarshalers =
        MappingMarshaler.createRepeated(profileData.getMappingTable());
    LocationMarshaler[] locationMarshalers =
        LocationMarshaler.createRepeated(profileData.getLocationTable());
    FunctionMarshaler[] functionMarshalers =
        FunctionMarshaler.createRepeated(profileData.getFunctionTable());
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(profileData.getAttributes());
    AttributeUnitMarshaler[] attributeUnitsMarshalers =
        AttributeUnitMarshaler.createRepeated(profileData.getAttributeUnits());
    LinkMarshaler[] linkMarshalers = LinkMarshaler.createRepeated(profileData.getLinkTable());
    ValueTypeMarshaler periodTypeMarshaler = ValueTypeMarshaler.create(profileData.getPeriodType());

    byte[][] convertedStrings = new byte[profileData.getStringTable().size()][];
    for (int i = 0; i < profileData.getStringTable().size(); i++) {
      convertedStrings[i] = profileData.getStringTable().get(i).getBytes(StandardCharsets.UTF_8);
    }

    int droppedAttributesCount =
        profileData.getTotalAttributeCount() - profileData.getAttributes().size();

    return new ProfileMarshaler(
        sampleTypeMarshalers,
        sampleMarshalers,
        mappingMarshalers,
        locationMarshalers,
        profileData.getLocationIndices(),
        functionMarshalers,
        attributeMarshalers,
        attributeUnitsMarshalers,
        linkMarshalers,
        convertedStrings,
        profileData.getTimeNanos(),
        profileData.getDurationNanos(),
        periodTypeMarshaler,
        profileData.getPeriod(),
        profileData.getCommentStrIndices(),
        profileData.getDefaultSampleTypeStringIndex(),
        profileData.getProfileIdBytes(),
        KeyValueMarshaler.createForAttributes(profileData.getAttributes()),
        droppedAttributesCount,
        MarshalerUtil.toBytes(profileData.getOriginalPayloadFormat()),
        profileData.getOriginalPayload());
  }

  private ProfileMarshaler(
      ValueTypeMarshaler[] sampleTypeMarshalers,
      SampleMarshaler[] sampleMarshalers,
      MappingMarshaler[] mappingTableMarshalers,
      LocationMarshaler[] locationTableMarshalers,
      List<Integer> locationIndices,
      FunctionMarshaler[] functionTableMarshalers,
      KeyValueMarshaler[] attributeTableMarshalers,
      AttributeUnitMarshaler[] attributeUnitMarshalers,
      LinkMarshaler[] linkTableMarshalers,
      byte[][] stringTableUtf8,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      List<Integer> comment,
      int defaultSampleType,
      byte[] profileId,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload) {
    super(
        calculateSize(
            sampleTypeMarshalers,
            sampleMarshalers,
            mappingTableMarshalers,
            locationTableMarshalers,
            locationIndices,
            functionTableMarshalers,
            attributeTableMarshalers,
            attributeUnitMarshalers,
            linkTableMarshalers,
            stringTableUtf8,
            timeNanos,
            durationNanos,
            periodTypeMarshaler,
            period,
            comment,
            defaultSampleType,
            profileId,
            attributeMarshalers,
            droppedAttributesCount,
            originalPayloadFormat,
            originalPayload));
    this.sampleTypeMarshalers = sampleTypeMarshalers;
    this.sampleMarshalers = sampleMarshalers;
    this.mappingTableMarshalers = mappingTableMarshalers;
    this.locationTableMarshalers = locationTableMarshalers;
    this.locationIndices = locationIndices;
    this.functionTableMarshalers = functionTableMarshalers;
    this.attributeTableMarshalers = attributeTableMarshalers;
    this.attributeUnitMarshalers = attributeUnitMarshalers;
    this.linkTableMarshalers = linkTableMarshalers;
    this.stringTable = stringTableUtf8;
    this.timeNanos = timeNanos;
    this.durationNanos = durationNanos;
    this.periodTypeMarshaler = periodTypeMarshaler;
    this.period = period;
    this.comment = comment;
    this.defaultSampleType = defaultSampleType;
    this.profileId = profileId;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
    this.originalPayloadFormatUtf8 = originalPayloadFormat;
    this.originalPayload = originalPayload;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Profile.SAMPLE_TYPE, sampleTypeMarshalers);
    output.serializeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    output.serializeRepeatedMessage(Profile.MAPPING_TABLE, mappingTableMarshalers);
    output.serializeRepeatedMessage(Profile.LOCATION_TABLE, locationTableMarshalers);
    output.serializeRepeatedInt32(Profile.LOCATION_INDICES, locationIndices);
    output.serializeRepeatedMessage(Profile.FUNCTION_TABLE, functionTableMarshalers);
    output.serializeRepeatedMessage(Profile.ATTRIBUTE_TABLE, attributeTableMarshalers);
    output.serializeRepeatedMessage(Profile.ATTRIBUTE_UNITS, attributeUnitMarshalers);
    output.serializeRepeatedMessage(Profile.LINK_TABLE, linkTableMarshalers);
    output.serializeRepeatedString(Profile.STRING_TABLE, stringTable);
    output.serializeInt64(Profile.TIME_NANOS, timeNanos);
    output.serializeInt64(Profile.DURATION_NANOS, durationNanos);
    output.serializeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    output.serializeInt64(Profile.PERIOD, period);
    output.serializeRepeatedInt32(Profile.COMMENT_STRINDICES, comment);
    output.serializeInt32(Profile.DEFAULT_SAMPLE_TYPE_STRINDEX, defaultSampleType);

    output.serializeBytes(Profile.PROFILE_ID, profileId);
    output.serializeRepeatedMessage(Profile.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(Profile.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeString(Profile.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormatUtf8);
    output.serializeByteBuffer(Profile.ORIGINAL_PAYLOAD, originalPayload);
  }

  private static int calculateSize(
      ValueTypeMarshaler[] sampleTypeMarshalers,
      SampleMarshaler[] sampleMarshalers,
      MappingMarshaler[] mappingMarshalers,
      LocationMarshaler[] locationMarshalers,
      List<Integer> locationIndices,
      FunctionMarshaler[] functionMarshalers,
      KeyValueMarshaler[] attributeTableMarshalers,
      AttributeUnitMarshaler[] attributeUnitMarshalers,
      LinkMarshaler[] linkMarshalers,
      byte[][] stringTable,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      List<Integer> comment,
      int defaultSampleType,
      byte[] profileId,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Profile.SAMPLE_TYPE, sampleTypeMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.MAPPING_TABLE, mappingMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.LOCATION_TABLE, locationMarshalers);
    size += MarshalerUtil.sizeRepeatedInt32(Profile.LOCATION_INDICES, locationIndices);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.FUNCTION_TABLE, functionMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.ATTRIBUTE_TABLE, attributeTableMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.ATTRIBUTE_UNITS, attributeUnitMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.LINK_TABLE, linkMarshalers);
    size += MarshalerUtil.sizeRepeatedString(Profile.STRING_TABLE, stringTable);
    size += MarshalerUtil.sizeInt64(Profile.TIME_NANOS, timeNanos);
    size += MarshalerUtil.sizeInt64(Profile.DURATION_NANOS, durationNanos);
    size += MarshalerUtil.sizeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    size += MarshalerUtil.sizeInt64(Profile.PERIOD, period);
    size += MarshalerUtil.sizeRepeatedInt32(Profile.COMMENT_STRINDICES, comment);
    size += MarshalerUtil.sizeInt64(Profile.DEFAULT_SAMPLE_TYPE_STRINDEX, defaultSampleType);

    size += MarshalerUtil.sizeBytes(Profile.PROFILE_ID, profileId);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.ATTRIBUTES, attributeMarshalers);
    size += MarshalerUtil.sizeInt32(Profile.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    size += MarshalerUtil.sizeBytes(Profile.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormat);
    size += MarshalerUtil.sizeByteBuffer(Profile.ORIGINAL_PAYLOAD, originalPayload);

    return size;
  }
}
