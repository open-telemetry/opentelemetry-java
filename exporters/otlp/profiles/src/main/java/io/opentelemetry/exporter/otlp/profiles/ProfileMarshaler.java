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

  private final ValueTypeMarshaler sampleTypeMarshaler;
  private final SampleMarshaler[] sampleMarshalers;
  private final long timeNanos;
  private final long durationNanos;
  private final ValueTypeMarshaler periodTypeMarshaler;
  private final long period;
  private final List<Integer> comment;
  private final byte[] profileId;
  private final List<Integer> attributeIndices;
  private final int droppedAttributesCount;
  private final byte[] originalPayloadFormatUtf8;
  private final ByteBuffer originalPayload;

  static ProfileMarshaler create(ProfileData profileData) {

    ValueTypeMarshaler sampleTypeMarshaler = ValueTypeMarshaler.create(profileData.getSampleType());
    SampleMarshaler[] sampleMarshalers = SampleMarshaler.createRepeated(profileData.getSamples());
    ValueTypeMarshaler periodTypeMarshaler = ValueTypeMarshaler.create(profileData.getPeriodType());

    int droppedAttributesCount =
        profileData.getTotalAttributeCount() - profileData.getAttributeIndices().size();

    return new ProfileMarshaler(
        sampleTypeMarshaler,
        sampleMarshalers,
        profileData.getTimeNanos(),
        profileData.getDurationNanos(),
        periodTypeMarshaler,
        profileData.getPeriod(),
        profileData.getCommentStrIndices(),
        profileData.getProfileIdBytes(),
        profileData.getAttributeIndices(),
        droppedAttributesCount,
        MarshalerUtil.toBytes(profileData.getOriginalPayloadFormat()),
        profileData.getOriginalPayload());
  }

  private ProfileMarshaler(
      ValueTypeMarshaler sampleTypeMarshaler,
      SampleMarshaler[] sampleMarshalers,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      List<Integer> comment,
      byte[] profileId,
      List<Integer> attributeIndices,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload) {
    super(
        calculateSize(
            sampleTypeMarshaler,
            sampleMarshalers,
            timeNanos,
            durationNanos,
            periodTypeMarshaler,
            period,
            comment,
            profileId,
            attributeIndices,
            droppedAttributesCount,
            originalPayloadFormat,
            originalPayload));
    this.sampleTypeMarshaler = sampleTypeMarshaler;
    this.sampleMarshalers = sampleMarshalers;
    this.timeNanos = timeNanos;
    this.durationNanos = durationNanos;
    this.periodTypeMarshaler = periodTypeMarshaler;
    this.period = period;
    this.comment = comment;
    this.profileId = profileId;
    this.attributeIndices = attributeIndices;
    this.droppedAttributesCount = droppedAttributesCount;
    this.originalPayloadFormatUtf8 = originalPayloadFormat;
    this.originalPayload = originalPayload;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeMessage(Profile.SAMPLE_TYPE, sampleTypeMarshaler);
    output.serializeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    output.serializeFixed64(Profile.TIME_UNIX_NANO, timeNanos);
    output.serializeInt64(Profile.DURATION_NANO, durationNanos);
    output.serializeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    output.serializeInt64(Profile.PERIOD, period);
    output.serializeRepeatedInt32(Profile.COMMENT_STRINDICES, comment);

    output.serializeBytes(Profile.PROFILE_ID, profileId);
    output.serializeRepeatedInt32(Profile.ATTRIBUTE_INDICES, attributeIndices);
    output.serializeUInt32(Profile.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeString(Profile.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormatUtf8);
    output.serializeByteBuffer(Profile.ORIGINAL_PAYLOAD, originalPayload);
  }

  private static int calculateSize(
      ValueTypeMarshaler sampleTypeMarshaler,
      SampleMarshaler[] sampleMarshalers,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      List<Integer> comment,
      byte[] profileId,
      List<Integer> attributeIndices,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeMessage(Profile.SAMPLE_TYPE, sampleTypeMarshaler);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    size += MarshalerUtil.sizeFixed64(Profile.TIME_UNIX_NANO, timeNanos);
    size += MarshalerUtil.sizeInt64(Profile.DURATION_NANO, durationNanos);
    size += MarshalerUtil.sizeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    size += MarshalerUtil.sizeInt64(Profile.PERIOD, period);
    size += MarshalerUtil.sizeRepeatedInt32(Profile.COMMENT_STRINDICES, comment);

    size += MarshalerUtil.sizeBytes(Profile.PROFILE_ID, profileId);
    size += MarshalerUtil.sizeRepeatedInt32(Profile.ATTRIBUTE_INDICES, attributeIndices);
    size += MarshalerUtil.sizeInt32(Profile.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    size += MarshalerUtil.sizeBytes(Profile.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormat);
    size += MarshalerUtil.sizeByteBuffer(Profile.ORIGINAL_PAYLOAD, originalPayload);

    return size;
  }
}
