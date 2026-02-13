/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp4.internal;

import io.opentelemetry.sdk.common.export.MessageWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;

class OkHttpHttpSuppressionTest extends AbstractOkHttpSuppressionTest<OkHttpHttpSender> {

  @Override
  void send(OkHttpHttpSender sender, Runnable onSuccess, Runnable onFailure) {
    byte[] content = "A".getBytes(StandardCharsets.UTF_8);
    MessageWriter requestBodyWriter =
        new MessageWriter() {
          @Override
          public void writeMessage(OutputStream output) throws IOException {
            output.write(content);
          }

          @Override
          public int getContentLength() {
            return content.length;
          }
        };
    sender.send(requestBodyWriter, (response) -> onSuccess.run(), (error) -> onFailure.run());
  }

  @Override
  OkHttpHttpSender createSender(String endpoint) {
    return new OkHttpHttpSender(
        URI.create(endpoint),
        "text/plain",
        null,
        Duration.ofNanos(10),
        Duration.ofNanos(10),
        Collections::emptyMap,
        null,
        null,
        null,
        null,
        null);
  }
}
