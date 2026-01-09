/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static io.opentelemetry.proto.collector.profiles.v1development.internal.ExportProfilesServiceRequest.DICTIONARY;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileDictionaryData;
import io.opentelemetry.proto.collector.profiles.v1development.internal.ExportProfilesServiceRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link Marshaler} to convert SDK {@link ProfileData} to OTLP ExportProfilesServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ProfilesRequestMarshaler extends MarshalerWithSize {

  private static final ProfileDictionaryData EMPTY_DICTIONARY_DATA =
      ImmutableProfileDictionaryData.create(
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList());

  private static final ProtoFieldInfo RESOURCE_PROFILES =
      ExportProfilesServiceRequest.RESOURCE_PROFILES;

  private final ResourceProfilesMarshaler[] resourceProfilesMarshalers;
  private final ProfileDictionaryMarshaler profileDictionaryMarshaler;

  /**
   * Returns a {@link ProfilesRequestMarshaler} that can be used to convert the provided {@link
   * ProfileData} into a serialized OTLP ExportProfilesServiceRequest.
   */
  public static ProfilesRequestMarshaler create(Collection<ProfileData> profileList) {
    // Verify all profiles in batch have identical dictionary
    ProfileDictionaryData profileDictionaryData = null;
    for (ProfileData profileData : profileList) {
      if (profileDictionaryData == null) {
        profileDictionaryData = profileData.getProfileDictionaryData();
      } else if (profileDictionaryData != profileData.getProfileDictionaryData()) {
        throw new IllegalArgumentException(
            "All profiles in batch must have identical ProfileDictionaryData");
      }
    }

    ProfileDictionaryMarshaler profileDictionaryMarshaler =
        ProfileDictionaryMarshaler.create(
            profileDictionaryData == null ? EMPTY_DICTIONARY_DATA : profileDictionaryData);

    return new ProfilesRequestMarshaler(
        ResourceProfilesMarshaler.create(profileList), profileDictionaryMarshaler);
  }

  private ProfilesRequestMarshaler(
      ResourceProfilesMarshaler[] resourceProfilesMarshalers,
      ProfileDictionaryMarshaler profileDictionaryMarshaler) {
    super(calculateSize(resourceProfilesMarshalers, profileDictionaryMarshaler));
    this.resourceProfilesMarshalers = resourceProfilesMarshalers;
    this.profileDictionaryMarshaler = profileDictionaryMarshaler;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(RESOURCE_PROFILES, resourceProfilesMarshalers);
    output.serializeMessage(DICTIONARY, profileDictionaryMarshaler);
  }

  private static int calculateSize(
      ResourceProfilesMarshaler[] resourceProfilesMarshalers,
      ProfileDictionaryMarshaler profileDictionaryMarshaler) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(RESOURCE_PROFILES, resourceProfilesMarshalers);
    size += MarshalerUtil.sizeMessage(DICTIONARY, profileDictionaryMarshaler);
    return size;
  }
}
