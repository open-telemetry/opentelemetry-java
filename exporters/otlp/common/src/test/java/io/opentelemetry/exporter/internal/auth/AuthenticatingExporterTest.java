/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporter;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
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
        protected void writeTo(Serializer output) throws IOException {}
      };

  @Test
  void export() throws Exception {
    OkHttpExporter<Marshaler> exporter =
        new OkHttpExporterBuilder<>("otlp", "test", server.httpUri().toASCIIString())
            .setAuthenticator(
                (j) -> {
                  Map<String, String> headers = new HashMap<>();
                  headers.put("Authorization", "auth");
                  j.accept(headers);
                })
            .build();

    server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
    server.enqueue(HttpResponse.of(HttpStatus.OK));

    CompletableResultCode result = exporter.export(marshaler, 0);

    assertNull(server.takeRequest().request().headers().get("Authorization"));
    assertEquals("auth", server.takeRequest().request().headers().get("Authorization"));

    result.join(1, TimeUnit.MINUTES);
    assertTrue(result.isSuccess());
  }

  /** Ensure that exporter gives up if a request is always considered UNAUTHORIZED. */
  @Test
  void export_giveup() throws Exception {
    OkHttpExporter<Marshaler> exporter =
        new OkHttpExporterBuilder<>("otlp", "test", server.httpUri().toASCIIString())
            .setAuthenticator(
                (j) -> {
                  server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
                })
            .build();
    server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
    assertFalse(exporter.export(marshaler, 0).join(1, TimeUnit.MINUTES).isSuccess());
  }
}
