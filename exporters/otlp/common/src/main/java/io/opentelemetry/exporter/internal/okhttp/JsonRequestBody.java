/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.okhttp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class JsonRequestBody extends RequestBody {
  private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

  private final byte[] preserializedJson;

  /** Creates a new {@link JsonRequestBody}. */
  public JsonRequestBody(Marshaler marshaler) {
    preserializedJson = preserializeJson(marshaler);
  }

  @Override
  public long contentLength() {
    return preserializedJson.length;
  }

  @Override
  public MediaType contentType() {
    return JSON_MEDIA_TYPE;
  }

  @Override
  public void writeTo(BufferedSink bufferedSink) throws IOException {
    bufferedSink.write(preserializedJson);
  }

  private static byte[] preserializeJson(Marshaler marshaler) {
    ByteArrayOutputStream jsonBos = new ByteArrayOutputStream();
    try {
      marshaler.writeJsonTo(jsonBos);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Serialization error, this is likely a bug in OpenTelemetry.", e);
    }
    return jsonBos.toByteArray();
  }
}
