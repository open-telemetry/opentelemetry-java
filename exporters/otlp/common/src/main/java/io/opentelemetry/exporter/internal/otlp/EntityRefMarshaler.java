/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.common.v1.internal.EntityRef;
import io.opentelemetry.sdk.resources.internal.Entity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

/**
 * A Marshaler of {@link io.opentelemetry.sdk.resources.internal.Entity}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class EntityRefMarshaler extends MarshalerWithSize {
  @Nullable private final byte[] schemaUrlUtf8;
  private final byte[] typeUtf8;
  private final byte[][] idKeysUtf8;
  private final byte[][] descriptionKeysUtf8;

  @Override
  protected void writeTo(Serializer output) throws IOException {
    if (schemaUrlUtf8 != null) {
      output.writeString(EntityRef.SCHEMA_URL, schemaUrlUtf8);
    }
    output.writeString(EntityRef.TYPE, typeUtf8);
    output.writeRepeatedString(EntityRef.ID_KEYS, idKeysUtf8);
    output.writeRepeatedString(EntityRef.DESCRIPTION_KEYS, descriptionKeysUtf8);
  }

  /** Consttructs an entity reference marshaler from a full entity. */
  static EntityRefMarshaler createForEntity(Entity e) {
    byte[] schemaUrlUtf8 = null;
    if (!StringUtils.isNullOrEmpty(e.getSchemaUrl())) {
      schemaUrlUtf8 = e.getSchemaUrl().getBytes(StandardCharsets.UTF_8);
    }
    return new EntityRefMarshaler(
        schemaUrlUtf8,
        e.getType().getBytes(StandardCharsets.UTF_8),
        e.getId().asMap().keySet().stream()
            .map(key -> key.getKey().getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new),
        e.getDescription().asMap().keySet().stream()
            .map(key -> key.getKey().getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new));
  }

  private EntityRefMarshaler(
      @Nullable byte[] schemaUrlUtf8,
      byte[] typeUtf8,
      byte[][] idKeysUtf8,
      byte[][] descriptionKeysUtf8) {
    super(calculateSize(schemaUrlUtf8, typeUtf8, idKeysUtf8, descriptionKeysUtf8));
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.typeUtf8 = typeUtf8;
    this.idKeysUtf8 = idKeysUtf8;
    this.descriptionKeysUtf8 = descriptionKeysUtf8;
  }

  private static int calculateSize(
      @Nullable byte[] schemaUrlUtf8,
      byte[] typeUtf8,
      byte[][] idKeysUtf8,
      byte[][] descriptionKeysUtf8) {
    int size = 0;
    if (schemaUrlUtf8 != null) {
      size += MarshalerUtil.sizeBytes(EntityRef.SCHEMA_URL, schemaUrlUtf8);
    }
    size += MarshalerUtil.sizeBytes(EntityRef.TYPE, typeUtf8);
    MarshalerUtil.sizeRepeatedString(EntityRef.ID_KEYS, idKeysUtf8);
    MarshalerUtil.sizeRepeatedString(EntityRef.DESCRIPTION_KEYS, descriptionKeysUtf8);
    return size;
  }
}
