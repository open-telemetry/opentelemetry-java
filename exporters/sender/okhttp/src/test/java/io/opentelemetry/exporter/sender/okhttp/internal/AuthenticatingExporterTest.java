/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;

/** Test Authentication in an exporter. */
@ExtendWith(MockitoExtension.class)
class AuthenticatingExporterTest {

  @RegisterExtension static final MockWebServerExtension server = new MockWebServerExtension();
  private final Marshaler marshaler =
      new Marshaler() {
        @Override
        public int getBinarySerializedSize() {
          return 0;
        }

        @Override
        protected void writeTo(Serializer output) {}
      };

  @Test
  void export() {
    HttpExporter<Marshaler> exporter =
        new HttpExporterBuilder<>("otlp", "test", server.httpUri().toASCIIString())
            .setAuthenticator(
                () -> {
                  Map<String, String> headers = new HashMap<>();
                  headers.put("Authorization", "auth");
                  return headers;
                })
            .build();

    server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
    server.enqueue(HttpResponse.of(HttpStatus.OK));

    CompletableResultCode result = exporter.export(marshaler, 0);

    assertThat(server.takeRequest().request().headers().get("Authorization")).isNull();
    assertThat(server.takeRequest().request().headers().get("Authorization")).isEqualTo("auth");

    result.join(1, TimeUnit.MINUTES);
    assertThat(result.isSuccess()).isTrue();
  }

  /** Ensure that exporter gives up if a request is always considered UNAUTHORIZED. */
  @Test
  @SuppressLogger(HttpExporter.class)
  void export_giveup() {
    HttpExporter<Marshaler> exporter =
        new HttpExporterBuilder<>("otlp", "test", server.httpUri().toASCIIString())
            .setAuthenticator(
                () -> {
                  server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
                  return Collections.emptyMap();
                })
            .exportAsJson()
            .build();
    server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
    assertThat(exporter.export(marshaler, 0).join(1, TimeUnit.MINUTES).isSuccess()).isFalse();
  }
}
