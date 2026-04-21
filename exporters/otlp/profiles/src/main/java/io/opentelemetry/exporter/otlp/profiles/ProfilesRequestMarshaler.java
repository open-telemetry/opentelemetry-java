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
import io.opentelemetry.proto.collector.profiles.v1development.internal.ExportProfilesServiceRequest;
import io.opentelemetry.sdk.profiles.data.ProfileData;
import io.opentelemetry.sdk.profiles.data.ProfilesDictionaryData;
import io.opentelemetry.sdk.profiles.internal.data.ImmutableProfilesDictionaryData;
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

  private static final ProfilesDictionaryData EMPTY_DICTIONARY_DATA =
      ImmutableProfilesDictionaryData.create(
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
  private final ProfilesDictionaryMarshaler profilesDictionaryMarshaler;

  /**
   * Returns a {@link ProfilesRequestMarshaler} that can be used to convert the provided {@link
   * ProfileData} into a serialized OTLP ExportProfilesServiceRequest.
   */
  public static ProfilesRequestMarshaler create(Collection<ProfileData> profileList) {
    // Verify all profiles in batch have identical dictionary
    ProfilesDictionaryData profilesDictionaryData = null;
    for (ProfileData profileData : profileList) {
      if (profilesDictionaryData == null) {
        profilesDictionaryData = profileData.getProfileDictionaryData();
      } else if (profilesDictionaryData != profileData.getProfileDictionaryData()) {
        throw new IllegalArgumentException(
            "All profiles in batch must have identical ProfileDictionaryData");
      }
    }

    ProfilesDictionaryMarshaler profilesDictionaryMarshaler =
        ProfilesDictionaryMarshaler.create(
            profilesDictionaryData == null ? EMPTY_DICTIONARY_DATA : profilesDictionaryData);

    return new ProfilesRequestMarshaler(
        ResourceProfilesMarshaler.create(profileList), profilesDictionaryMarshaler);
  }

  private ProfilesRequestMarshaler(
      ResourceProfilesMarshaler[] resourceProfilesMarshalers,
      ProfilesDictionaryMarshaler profilesDictionaryMarshaler) {
    super(calculateSize(resourceProfilesMarshalers, profilesDictionaryMarshaler));
    this.resourceProfilesMarshalers = resourceProfilesMarshalers;
    this.profilesDictionaryMarshaler = profilesDictionaryMarshaler;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(RESOURCE_PROFILES, resourceProfilesMarshalers);
    output.serializeMessage(DICTIONARY, profilesDictionaryMarshaler);
  }

  private static int calculateSize(
      ResourceProfilesMarshaler[] resourceProfilesMarshalers,
      ProfilesDictionaryMarshaler profilesDictionaryMarshaler) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(RESOURCE_PROFILES, resourceProfilesMarshalers);
    size += MarshalerUtil.sizeMessage(DICTIONARY, profilesDictionaryMarshaler);
    return size;
  }
}
