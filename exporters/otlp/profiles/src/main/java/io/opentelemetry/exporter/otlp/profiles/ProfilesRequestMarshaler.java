/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.collector.profiles.v1development.internal.ExportProfilesServiceRequest;
import java.io.IOException;
import java.util.Collection;

/**
 * {@link Marshaler} to convert SDK {@link ProfileData} to OTLP ExportProfilesServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ProfilesRequestMarshaler extends MarshalerWithSize {

  private static final ProtoFieldInfo RESOURCE_PROFILES =
      ExportProfilesServiceRequest.RESOURCE_PROFILES;

  private final ResourceProfilesMarshaler[] resourceProfilesMarshalers;

  /**
   * Returns a {@link ProfilesRequestMarshaler} that can be used to convert the provided {@link
   * ProfileData} into a serialized OTLP ExportProfilesServiceRequest.
   */
  public static ProfilesRequestMarshaler create(Collection<ProfileData> profileList) {
    return new ProfilesRequestMarshaler(ResourceProfilesMarshaler.create(profileList));
  }

  private ProfilesRequestMarshaler(ResourceProfilesMarshaler[] resourceProfilesMarshalers) {
    super(MarshalerUtil.sizeRepeatedMessage(RESOURCE_PROFILES, resourceProfilesMarshalers));
    this.resourceProfilesMarshalers = resourceProfilesMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(RESOURCE_PROFILES, resourceProfilesMarshalers);
  }
}
