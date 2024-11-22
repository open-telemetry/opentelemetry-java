/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.profiles.v1experimental.internal.ProfileContainer;
import java.io.IOException;
import java.nio.ByteBuffer;

final class ProfileContainerMarshaler extends MarshalerWithSize {

  private final byte[] profileId;
  private final long startEpochNanos;
  private final long endEpochNanos;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final int droppedAttributesCount;
  private final byte[] originalPayloadFormatUtf8;
  private final ByteBuffer originalPayload;
  private final ProfileMarshaler profileMarshaler;

  static ProfileContainerMarshaler create(ProfileContainerData profileContainerData) {
    int droppedAttributesCount =
        profileContainerData.getTotalAttributeCount() - profileContainerData.getAttributes().size();

    ByteBuffer originalPayload = profileContainerData.getOriginalPayload();
    if (originalPayload == null) {
      originalPayload = ByteBuffer.allocate(0);
    } else {
      originalPayload = originalPayload.duplicate().asReadOnlyBuffer();
    }

    return new ProfileContainerMarshaler(
        profileContainerData.getProfileIdBytes(),
        profileContainerData.getStartEpochNanos(),
        profileContainerData.getEndEpochNanos(),
        KeyValueMarshaler.createForAttributes(profileContainerData.getAttributes()),
        droppedAttributesCount,
        MarshalerUtil.toBytes(profileContainerData.getOriginalPayloadFormat()),
        originalPayload,
        ProfileMarshaler.create(profileContainerData.getProfile()));
  }

  private ProfileContainerMarshaler(
      byte[] profileId,
      long startEpochNanos,
      long endEpochNanos,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload,
      ProfileMarshaler profileMarshaler) {
    super(
        calculateSize(
            profileId,
            startEpochNanos,
            endEpochNanos,
            attributeMarshalers,
            droppedAttributesCount,
            originalPayloadFormat,
            originalPayload,
            profileMarshaler));
    this.profileId = profileId;
    this.startEpochNanos = startEpochNanos;
    this.endEpochNanos = endEpochNanos;
    this.attributeMarshalers = attributeMarshalers;
    this.droppedAttributesCount = droppedAttributesCount;
    this.originalPayloadFormatUtf8 = originalPayloadFormat;
    this.originalPayload = originalPayload;
    this.profileMarshaler = profileMarshaler;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeBytes(ProfileContainer.PROFILE_ID, profileId);
    output.serializeFixed64(ProfileContainer.START_TIME_UNIX_NANO, startEpochNanos);
    output.serializeFixed64(ProfileContainer.END_TIME_UNIX_NANO, endEpochNanos);
    output.serializeRepeatedMessage(ProfileContainer.ATTRIBUTES, attributeMarshalers);
    output.serializeUInt32(ProfileContainer.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    output.serializeString(ProfileContainer.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormatUtf8);
    output.serializeByteBuffer(ProfileContainer.ORIGINAL_PAYLOAD, originalPayload);
    output.serializeMessage(ProfileContainer.PROFILE, profileMarshaler);
  }

  private static int calculateSize(
      byte[] profileId,
      long startEpochNanos,
      long endEpochNanos,
      KeyValueMarshaler[] attributeMarshalers,
      int droppedAttributesCount,
      byte[] originalPayloadFormat,
      ByteBuffer originalPayload,
      ProfileMarshaler profileMarshaler) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeBytes(ProfileContainer.PROFILE_ID, profileId);
    size += MarshalerUtil.sizeFixed64(ProfileContainer.START_TIME_UNIX_NANO, startEpochNanos);
    size += MarshalerUtil.sizeFixed64(ProfileContainer.END_TIME_UNIX_NANO, endEpochNanos);
    size += MarshalerUtil.sizeRepeatedMessage(ProfileContainer.ATTRIBUTES, attributeMarshalers);
    size +=
        MarshalerUtil.sizeUInt32(ProfileContainer.DROPPED_ATTRIBUTES_COUNT, droppedAttributesCount);
    size +=
        MarshalerUtil.sizeBytes(ProfileContainer.ORIGINAL_PAYLOAD_FORMAT, originalPayloadFormat);
    size += MarshalerUtil.sizeByteBuffer(ProfileContainer.ORIGINAL_PAYLOAD, originalPayload);
    size += MarshalerUtil.sizeMessage(ProfileContainer.PROFILE, profileMarshaler);
    return size;
  }
}
