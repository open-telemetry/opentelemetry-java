/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.profiles.v1development.internal.ResourceProfiles;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class ResourceProfilesMarshaler extends MarshalerWithSize {

  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrl;
  private final InstrumentationScopeProfilesMarshaler[] instrumentationScopeProfilesMarshalers;

  /** Returns Marshalers of ResourceProfiles created by grouping the provided Profiles. */
  @SuppressWarnings("AvoidObjectArrays")
  static ResourceProfilesMarshaler[] create(Collection<ProfileData> profiles) {
    Map<Resource, Map<InstrumentationScopeInfo, List<ProfileMarshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(profiles);

    ResourceProfilesMarshaler[] resourceProfilesMarshalers =
        new ResourceProfilesMarshaler[resourceAndScopeMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<ProfileMarshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      InstrumentationScopeProfilesMarshaler[] instrumentationScopeProfilesMarshalers =
          new InstrumentationScopeProfilesMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;

      for (Map.Entry<InstrumentationScopeInfo, List<ProfileMarshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationScopeProfilesMarshalers[posInstrumentation++] =
            new InstrumentationScopeProfilesMarshaler(
                InstrumentationScopeMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }

      resourceProfilesMarshalers[posResource++] =
          new ResourceProfilesMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationScopeProfilesMarshalers);
    }

    return resourceProfilesMarshalers;
  }

  private ResourceProfilesMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeProfilesMarshaler[] instrumentationScopeProfilesMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrl, instrumentationScopeProfilesMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationScopeProfilesMarshalers = instrumentationScopeProfilesMarshalers;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceProfiles.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceProfiles.SCOPE_PROFILES, instrumentationScopeProfilesMarshalers);
    output.serializeString(ResourceProfiles.SCHEMA_URL, schemaUrl);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeProfilesMarshaler[] instrumentationScopeProfilesMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceProfiles.RESOURCE, resourceMarshaler);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceProfiles.SCOPE_PROFILES, instrumentationScopeProfilesMarshalers);
    size += MarshalerUtil.sizeBytes(ResourceProfiles.SCHEMA_URL, schemaUrl);
    return size;
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<ProfileMarshaler>>>
      groupByResourceAndScope(Collection<ProfileData> profiles) {
    return MarshalerUtil.groupByResourceAndScope(
        profiles,
        ProfileData::getResource,
        ProfileData::getInstrumentationScopeInfo,
        ProfileMarshaler::create);
  }
}
