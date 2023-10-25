package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.http.HttpSender;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Consumer;

class OkHttpHttpSenderTest extends OkHttpSenderTest<HttpSender> {

  @Override
  void send(HttpSender sender, Runnable onSuccess, Runnable onFailure) {
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
  HttpSender createSender(String endpoint) {
    return new OkHttpHttpSender(
        endpoint, false, "text/plain", 10L, Collections::emptyMap, null, null, null, null);
  }
}
