/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoEnumInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.trace.v1.internal.Status;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;

final class SpanStatusMarshaler extends MarshalerWithSize {
  private final ProtoEnumInfo protoStatusCode;
  private final ProtoEnumInfo deprecatedStatusCode;
  private final byte[] descriptionUtf8;

  static SpanStatusMarshaler create(StatusData status) {
    ProtoEnumInfo protoStatusCode = Status.StatusCode.STATUS_CODE_UNSET;
    ProtoEnumInfo deprecatedStatusCode = Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK;
    if (status.getStatusCode() == StatusCode.OK) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_OK;
    } else if (status.getStatusCode() == StatusCode.ERROR) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR;
      deprecatedStatusCode = Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;
    }
    byte[] description = MarshalerUtil.toBytes(status.getDescription());
    return new SpanStatusMarshaler(protoStatusCode, deprecatedStatusCode, description);
  }

  private SpanStatusMarshaler(
      ProtoEnumInfo protoStatusCode, ProtoEnumInfo deprecatedStatusCode, byte[] descriptionUtf8) {
    super(computeSize(protoStatusCode, deprecatedStatusCode, descriptionUtf8));
    this.protoStatusCode = protoStatusCode;
    this.deprecatedStatusCode = deprecatedStatusCode;
    this.descriptionUtf8 = descriptionUtf8;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeEnum(Status.DEPRECATED_CODE, deprecatedStatusCode);
    output.serializeString(Status.MESSAGE, descriptionUtf8);
    output.serializeEnum(Status.CODE, protoStatusCode);
  }

  private static int computeSize(
      ProtoEnumInfo protoStatusCode, ProtoEnumInfo deprecatedStatusCode, byte[] descriptionUtf8) {
    int size = 0;
    size += MarshalerUtil.sizeEnum(Status.DEPRECATED_CODE, deprecatedStatusCode);
    size += MarshalerUtil.sizeBytes(Status.MESSAGE, descriptionUtf8);
    size += MarshalerUtil.sizeEnum(Status.CODE, protoStatusCode);
    return size;
  }
}
