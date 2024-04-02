/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.trace.v1.internal.Status;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;

final class SpanStatusMarshaler extends MarshalerWithSize {
  private final ProtoEnumInfo protoStatusCode;
  private final byte[] descriptionUtf8;

  static SpanStatusMarshaler create(StatusData status) {
    ProtoEnumInfo protoStatusCode = Status.StatusCode.STATUS_CODE_UNSET;
    if (status.getStatusCode() == StatusCode.OK) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_OK;
    } else if (status.getStatusCode() == StatusCode.ERROR) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR;
    }
    byte[] description = MarshalerUtil.toBytes(status.getDescription());
    return new SpanStatusMarshaler(protoStatusCode, description);
  }

  private SpanStatusMarshaler(ProtoEnumInfo protoStatusCode, byte[] descriptionUtf8) {
    super(calculateSize(protoStatusCode, descriptionUtf8));
    this.protoStatusCode = protoStatusCode;
    this.descriptionUtf8 = descriptionUtf8;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeString(Status.MESSAGE, descriptionUtf8);
    output.serializeEnum(Status.CODE, protoStatusCode);
  }

  public static void writeTo(Serializer output, StatusData status, MarshalerContext context)
      throws IOException {
    ProtoEnumInfo protoStatusCode = Status.StatusCode.STATUS_CODE_UNSET;
    if (status.getStatusCode() == StatusCode.OK) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_OK;
    } else if (status.getStatusCode() == StatusCode.ERROR) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR;
    }

    byte[] descriptionUtf8 = context.getByteArray();

    output.serializeString(Status.MESSAGE, descriptionUtf8);
    output.serializeEnum(Status.CODE, protoStatusCode);
  }

  private static int calculateSize(ProtoEnumInfo protoStatusCode, byte[] descriptionUtf8) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(Status.MESSAGE, descriptionUtf8);
    size += MarshalerUtil.sizeEnum(Status.CODE, protoStatusCode);
    return size;
  }

  public static int calculateSize(StatusData status, MarshalerContext context) {
    ProtoEnumInfo protoStatusCode = Status.StatusCode.STATUS_CODE_UNSET;
    if (status.getStatusCode() == StatusCode.OK) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_OK;
    } else if (status.getStatusCode() == StatusCode.ERROR) {
      protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR;
    }
    byte[] descriptionUtf8 = MarshalerUtil.toBytes(status.getDescription());
    context.addData(descriptionUtf8);

    int size = 0;
    size += MarshalerUtil.sizeBytes(Status.MESSAGE, descriptionUtf8);
    size += MarshalerUtil.sizeEnum(Status.CODE, protoStatusCode);

    return size;
  }
}
