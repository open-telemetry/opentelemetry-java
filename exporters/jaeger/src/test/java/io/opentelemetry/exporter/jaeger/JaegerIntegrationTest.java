/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class JaegerIntegrationTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final OkHttpClient client = new OkHttpClient();

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final String SERVICE_NAME = "E2E-test";
  private static final String JAEGER_URL = "http://localhost";
  private final Tracer tracer = OpenTelemetry.getGlobalTracer(getClass().getCanonicalName());

  @Container
  public static GenericContainer<?> jaegerContainer =
      new GenericContainer<>("open-telemetry-docker-dev.bintray.io/java-test-containers:jaeger")
          .withExposedPorts(COLLECTOR_PORT, QUERY_PORT)
          .waitingFor(new HttpWaitStrategy().forPath("/"));

  @Test
  void testJaegerIntegration() {
    setupJaegerExporter();
    imitateWork();
    Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .until(JaegerIntegrationTest::assertJaegerHaveTrace);
  }

  private static void setupJaegerExporter() {
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress("127.0.0.1", jaegerContainer.getMappedPort(COLLECTOR_PORT))
            .usePlaintext()
            .build();
    SpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.builder()
            .setServiceName(SERVICE_NAME)
            .setChannel(jaegerChannel)
            .setDeadlineMs(30000)
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

  private static boolean assertJaegerHaveTrace() {
    try {
      String url =
          String.format(
              "%s/api/traces?service=%s",
              String.format(JAEGER_URL + ":%d", jaegerContainer.getMappedPort(QUERY_PORT)),
              SERVICE_NAME);

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
