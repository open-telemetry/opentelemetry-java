/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Consumer;

class OkHttpHttpSuppressionTest extends AbstractOkHttpSuppressionTest<OkHttpHttpSender> {

  @Override
  void send(OkHttpHttpSender sender, Runnable onSuccess, Runnable onFailure) {
    byte[] content = "A".getBytes(StandardCharsets.UTF_8);
    Consumer<OutputStream> outputStreamConsumer =
        outputStream -> {
          try {
            outputStream.write(content);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        };
    sender.send(
        outputStreamConsumer,
        content.length,
        (response) -> onSuccess.run(),
        (error) -> onFailure.run());
  }

  @Override
  OkHttpHttpSender createSender(String endpoint) {
    return new OkHttpHttpSender(
        endpoint, null, "text/plain", 10L, Collections::emptyMap, null, null, null, null);
  }
}
