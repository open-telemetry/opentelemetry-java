/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.Profile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

final class ProfileMarshaler extends MarshalerWithSize {

  private final ValueTypeMarshaler[] sampleTypeMarshalers;
  private final SampleMarshaler[] sampleMarshalers;
  private final List<Integer> locationIndices;
  private final long timeNanos;
  private final long durationNanos;
  private final ValueTypeMarshaler periodTypeMarshaler;
  private final long period;
  private final List<Integer> comment;
  private final int defaultSampleType;
  private final byte[] profileId;
  private final List<Integer> attributeIndices;
  private final int droppedAttributesCount;
  private final byte[] originalPayloadFormatUtf8;
  private final ByteBuffer originalPayload;

  static ProfileMarshaler create(ProfileData profileData) {

    ValueTypeMarshaler[] sampleTypeMarshalers =
        ValueTypeMarshaler.createRepeated(profileData.getSampleTypes());
    SampleMarshaler[] sampleMarshalers = SampleMarshaler.createRepeated(profileData.getSamples());
    ValueTypeMarshaler periodTypeMarshaler = ValueTypeMarshaler.create(profileData.getPeriodType());

    int droppedAttributesCount =
        profileData.getTotalAttributeCount() - profileData.getAttributeIndices().size();

    return new ProfileMarshaler(
        sampleTypeMarshalers,
        sampleMarshalers,
        profileData.getLocationIndices(),
        profileData.getTimeNanos(),
        profileData.getDurationNanos(),
        periodTypeMarshaler,
        profileData.getPeriod(),
        profileData.getCommentStrIndices(),
        profileData.getDefaultSampleTypeStringIndex(),
        profileData.getProfileIdBytes(),
        profileData.getAttributeIndices(),
        droppedAttributesCount,
        MarshalerUtil.toBytes(profileData.getOriginalPayloadFormat()),
        profileData.getOriginalPayload());
  }

  private ProfileMarshaler(
      ValueTypeMarshaler[] sampleTypeMarshalers,
      SampleMarshaler[] sampleMarshalers,
      List<Integer> locationIndices,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      List<Integer> comment,
      int defaultSampleType,
      byte[] profileId,
      List<Integer> attributeIndices,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload) {
    super(
        calculateSize(
            sampleTypeMarshalers,
            sampleMarshalers,
            locationIndices,
            timeNanos,
            durationNanos,
            periodTypeMarshaler,
            period,
            comment,
            defaultSampleType,
            profileId,
            attributeIndices,
            droppedAttributesCount,
            originalPayloadFormat,
            originalPayload));
    this.sampleTypeMarshalers = sampleTypeMarshalers;
    this.sampleMarshalers = sampleMarshalers;
    this.locationIndices = locationIndices;
    this.timeNanos = timeNanos;
    this.durationNanos = durationNanos;
    this.periodTypeMarshaler = periodTypeMarshaler;
    this.period = period;
    this.comment = comment;
    this.defaultSampleType = defaultSampleType;
    this.profileId = profileId;
    this.attributeIndices = attributeIndices;
    this.droppedAttributesCount = droppedAttributesCount;
    this.originalPayloadFormatUtf8 = originalPayloadFormat;
    this.originalPayload = originalPayload;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Profile.SAMPLE_TYPE, sampleTypeMarshalers);
    output.serializeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    output.serializeRepeatedInt32(Profile.LOCATION_INDICES, locationIndices);
    output.serializeInt64(Profile.TIME_NANOS, timeNanos);
    output.serializeInt64(Profile.DURATION_NANOS, durationNanos);
    output.serializeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    output.serializeInt64(Profile.PERIOD, period);
    output.serializeRepeatedInt32(Profile.COMMENT_STRINDICES, comment);
    output.serializeInt32(Profile.DEFAULT_SAMPLE_TYPE_INDEX, defaultSampleType);

    output.serializeBytes(Profile.PROFILE_ID, profileId);
    output.serializeRepeatedInt32(Profile.ATTRIBUTE_INDICES, attributeIndices);
    output.serializeUInt32(Profile.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeString(Profile.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormatUtf8);
    output.serializeByteBuffer(Profile.ORIGINAL_PAYLOAD, originalPayload);
  }

  private static int calculateSize(
      ValueTypeMarshaler[] sampleTypeMarshalers,
      SampleMarshaler[] sampleMarshalers,
      List<Integer> locationIndices,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      List<Integer> comment,
      int defaultSampleType,
      byte[] profileId,
      List<Integer> attributeIndices,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Profile.SAMPLE_TYPE, sampleTypeMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    size += MarshalerUtil.sizeRepeatedInt32(Profile.LOCATION_INDICES, locationIndices);
    size += MarshalerUtil.sizeInt64(Profile.TIME_NANOS, timeNanos);
    size += MarshalerUtil.sizeInt64(Profile.DURATION_NANOS, durationNanos);
    size += MarshalerUtil.sizeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    size += MarshalerUtil.sizeInt64(Profile.PERIOD, period);
    size += MarshalerUtil.sizeRepeatedInt32(Profile.COMMENT_STRINDICES, comment);
    size += MarshalerUtil.sizeInt64(Profile.DEFAULT_SAMPLE_TYPE_INDEX, defaultSampleType);

    size += MarshalerUtil.sizeBytes(Profile.PROFILE_ID, profileId);
    size += MarshalerUtil.sizeRepeatedInt32(Profile.ATTRIBUTE_INDICES, attributeIndices);
    size += MarshalerUtil.sizeInt32(Profile.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    size += MarshalerUtil.sizeBytes(Profile.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormat);
    size += MarshalerUtil.sizeByteBuffer(Profile.ORIGINAL_PAYLOAD, originalPayload);

    return size;
  }
}
