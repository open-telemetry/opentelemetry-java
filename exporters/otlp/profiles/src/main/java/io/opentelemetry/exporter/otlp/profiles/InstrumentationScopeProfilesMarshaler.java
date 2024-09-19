/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.profiles.v1experimental.internal.ScopeProfiles;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeProfilesMarshaler extends MarshalerWithSize {

  private final InstrumentationScopeMarshaler instrumentationScope;
  private final List<ProfileContainerMarshaler> profileContainerMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationScopeProfilesMarshaler(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<ProfileContainerMarshaler> profileContainerMarshalers) {
    super(calculateSize(instrumentationScope, schemaUrlUtf8, profileContainerMarshalers));
    this.instrumentationScope = instrumentationScope;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.profileContainerMarshalers = profileContainerMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ScopeProfiles.SCOPE, instrumentationScope);
    output.serializeRepeatedMessage(ScopeProfiles.PROFILES, profileContainerMarshalers);
    output.serializeString(ScopeProfiles.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<ProfileContainerMarshaler> profileContainerMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeProfiles.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeRepeatedMessage(ScopeProfiles.PROFILES, profileContainerMarshalers);
    size += MarshalerUtil.sizeBytes(ScopeProfiles.SCHEMA_URL, schemaUrlUtf8);
    return size;
  }
}
