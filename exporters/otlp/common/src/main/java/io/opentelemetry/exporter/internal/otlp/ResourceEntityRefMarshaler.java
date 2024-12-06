/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.experimental.ResourceEntityRefExperimental;
import io.opentelemetry.sdk.resources.Entity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

/**
 * A Marshaler of {@link io.opentelemetry.sdk.resources.Entity}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceEntityRefMarshaler extends MarshalerWithSize {
  @Nullable private final byte[] schemaUrlUtf8;
  private final byte[] typeUtf8;
  private final byte[][] identityAttributeKeysUtf8;
  private final byte[][] descriptiveAttributeKeysUtf8;

  @Override
  protected void writeTo(Serializer output) throws IOException {
    if (schemaUrlUtf8 != null) {
      output.writeString(ResourceEntityRefExperimental.SCHEMA_URL, schemaUrlUtf8);
    }
    output.writeString(ResourceEntityRefExperimental.TYPE, typeUtf8);
    output.writeRepeatedString(
        ResourceEntityRefExperimental.IDENTITY_ATTRIBUTES, identityAttributeKeysUtf8);
    output.writeRepeatedString(
        ResourceEntityRefExperimental.DESCRIPTION_ATTRIBUTES, descriptiveAttributeKeysUtf8);
  }

  public static ResourceEntityRefMarshaler createForEntity(Entity e) {
    byte[] schemaUrlUtf8 = null;
    if (!StringUtils.isNullOrEmpty(e.getSchemaUrl())) {
      schemaUrlUtf8 = e.getSchemaUrl().getBytes(StandardCharsets.UTF_8);
    }
    return new ResourceEntityRefMarshaler(
        schemaUrlUtf8,
        e.getType().getBytes(StandardCharsets.UTF_8),
        e.getIdentifyingAttributes().asMap().keySet().stream()
            .map(key -> key.getKey().getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new),
        e.getAttributes().asMap().keySet().stream()
            .map(key -> key.getKey().getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new));
  }

  private ResourceEntityRefMarshaler(
      @Nullable byte[] schemaUrlUtf8,
      byte[] typeUtf8,
      byte[][] identityAttributeKeysUtf8,
      byte[][] descriptiveAttributeKeysUtf8) {
    super(
        calculateSize(
            schemaUrlUtf8, typeUtf8, identityAttributeKeysUtf8, descriptiveAttributeKeysUtf8));
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.typeUtf8 = typeUtf8;
    this.identityAttributeKeysUtf8 = identityAttributeKeysUtf8;
    this.descriptiveAttributeKeysUtf8 = descriptiveAttributeKeysUtf8;
  }

  private static int calculateSize(
      @Nullable byte[] schemaUrlUtf8,
      byte[] typeUtf8,
      byte[][] identityAttributeKeysUtf8,
      byte[][] descriptiveAttributeKeysUtf8) {
    int size = 0;
    if (schemaUrlUtf8 != null) {
      size += MarshalerUtil.sizeBytes(ResourceEntityRefExperimental.SCHEMA_URL, schemaUrlUtf8);
    }
    size += MarshalerUtil.sizeBytes(ResourceEntityRefExperimental.TYPE, typeUtf8);
    // TODO - we need repeated string support.
    for (byte[] keyUtf8 : identityAttributeKeysUtf8) {
      size += MarshalerUtil.sizeBytes(ResourceEntityRefExperimental.IDENTITY_ATTRIBUTES, keyUtf8);
    }
    for (byte[] keyUtf8 : descriptiveAttributeKeysUtf8) {
      size +=
          MarshalerUtil.sizeBytes(ResourceEntityRefExperimental.DESCRIPTION_ATTRIBUTES, keyUtf8);
    }
    return size;
  }
}
