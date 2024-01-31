/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

class OkHttpHttpSuppressionTest extends AbstractOkHttpSuppressionTest<OkHttpHttpSender> {

  @Override
  void send(OkHttpHttpSender sender, Runnable onSuccess, Runnable onFailure) {
    byte[] content = "A".getBytes(StandardCharsets.UTF_8);
    Marshaler marshaler =
        new Marshaler() {
          @Override
          public int getBinarySerializedSize() {
            return content.length;
          }

          @Override
          protected void writeTo(Serializer output) throws IOException {
            output.serializeBytes(ProtoFieldInfo.create(1, 1, "field"), content);
          }
        };
    sender.send(
        marshaler, content.length, (response) -> onSuccess.run(), (error) -> onFailure.run());
  }

  @Override
  OkHttpHttpSender createSender(String endpoint) {
    return new OkHttpHttpSender(
        endpoint,
        null,
        false,
        "text/plain",
        10L,
        10L,
        Collections::emptyMap,
        null,
        null,
        null,
        null);
  }
}
