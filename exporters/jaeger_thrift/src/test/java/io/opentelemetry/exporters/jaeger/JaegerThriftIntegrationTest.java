/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.jaeger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class JaegerThriftIntegrationTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final OkHttpClient client = new OkHttpClient();

  private static final int QUERY_PORT = 16686;
  private static final int THRIFT_HTTP_PORT = 14268;
  private static final String JAEGER_VERSION = "1.17";
  private static final String SERVICE_NAME = "E2E-test";
  private static final String JAEGER_URL = "http://localhost";
  private final Tracer tracer = OpenTelemetry.getGlobalTracer(getClass().getCanonicalName());

  @Container
  public static GenericContainer<?> jaegerContainer =
      new GenericContainer<>(DockerImageName.parse("jaegertracing/all-in-one:" + JAEGER_VERSION))
          .withExposedPorts(THRIFT_HTTP_PORT, QUERY_PORT)
          .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
          .waitingFor(new HttpWaitStrategy().forPath("/"));

  @Test
  void testJaegerIntegration() {
    setupJaegerExporter();
    imitateWork();
    Awaitility.await()
        .atMost(30, TimeUnit.MINUTES)
        .until(JaegerThriftIntegrationTest::assertJaegerHasATrace);
  }

  private static void setupJaegerExporter() {
    Integer mappedPort = jaegerContainer.getMappedPort(THRIFT_HTTP_PORT);

    SpanExporter jaegerExporter =
        JaegerThriftSpanExporter.builder()
            .setServiceName(SERVICE_NAME)
            .setEndpoint(JAEGER_URL + ":" + mappedPort + "/api/traces")
            .build();
    OpenTelemetrySdk.getGlobalTracerManagement()
        .addSpanProcessor(SimpleSpanProcessor.builder(jaegerExporter).build());
  }

  private void imitateWork() {
    Span span = this.tracer.spanBuilder("Test span").startSpan();
    span.addEvent("some event");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    span.end();
  }

  private static boolean assertJaegerHasATrace() {
    try {
      Integer mappedPort = jaegerContainer.getMappedPort(QUERY_PORT);
      String url =
          String.format(
              "%s/api/traces?service=%s",
              String.format(JAEGER_URL + ":%d", mappedPort), SERVICE_NAME);

      Request request =
          new Request.Builder()
              .url(url)
              .header("Content-Type", "application/json")
              .header("Accept", "application/json")
              .build();

      final JsonNode json;
      try (Response response = client.newCall(request).execute()) {
        json = objectMapper.readTree(response.body().byteStream());
      }

      return json.get("data").get(0).get("traceID") != null;
    } catch (Exception e) {
      return false;
    }
  }
}
