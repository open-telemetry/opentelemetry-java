/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.GlobalOpenTelemetry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ZipkinConfigTest {

  private static final BlockingQueue<String> zipkinJsonRequests = new LinkedBlockingDeque<>();

  @RegisterExtension
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          // Zipkin
          sb.service(
              "/api/v2/spans",
              (ctx, req) ->
                  HttpResponse.from(
                      req.aggregate()
                          .thenApply(
                              aggRes -> {
                                zipkinJsonRequests.add(aggRes.contentUtf8());
                                return HttpResponse.of(HttpStatus.OK);
                              })));
        }
      };

  @BeforeEach
  void setUp() {
    zipkinJsonRequests.clear();
  }

  @Test
  void configures() {
    String endpoint = "localhost:" + server.httpPort();

    System.setProperty("otel.exporter.zipkin.endpoint", "http://" + endpoint + "/api/v2/spans");

    OpenTelemetrySdkAutoConfiguration.initialize();

    GlobalOpenTelemetry.get().getTracer("test").spanBuilder("test").startSpan().end();

    await().untilAsserted(() -> assertThat(zipkinJsonRequests).hasSize(1));
  }
}
