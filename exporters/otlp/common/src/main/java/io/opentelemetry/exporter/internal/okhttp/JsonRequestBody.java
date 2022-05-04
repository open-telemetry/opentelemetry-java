/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.okhttp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class JsonRequestBody extends RequestBody {
  private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

  private final byte[] preserializedJson;
  private final int contentLength;

  /** Creates a new {@link JsonRequestBody}. */
  public JsonRequestBody(Marshaler marshaler) {
    preserializedJson =
        MarshalerUtil.preserializeJsonFields(marshaler).getBytes(StandardCharsets.UTF_8);
    contentLength = preserializedJson.length;
  }

  @Override
  public long contentLength() {
    return contentLength;
  }

  @Override
  public MediaType contentType() {
    return JSON_MEDIA_TYPE;
  }

  @Override
  public void writeTo(BufferedSink bufferedSink) throws IOException {
    bufferedSink.write(preserializedJson);
  }
}
