/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.thrift;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.time.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class JaegerThriftIntegrationTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final OkHttpClient client = new OkHttpClient();

  private static final int QUERY_PORT = 16686;
  private static final int THRIFT_HTTP_PORT = 14268;
  private static final int HEALTH_PORT = 14269;
  private static final String SERVICE_NAME = "E2E-test";
  private static final String JAEGER_URL = "http://localhost";

  @Container
  public static GenericContainer<?> jaegerContainer =
      new GenericContainer<>("ghcr.io/open-telemetry/java-test-containers:jaeger")
          .withExposedPorts(THRIFT_HTTP_PORT, QUERY_PORT, HEALTH_PORT)
          .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("jaeger")))
          .waitingFor(Wait.forHttp("/").forPort(HEALTH_PORT));

  @Test
  void testJaegerIntegration() {
    OpenTelemetry openTelemetry = initOpenTelemetry();
    imitateWork(openTelemetry);
    Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .until(JaegerThriftIntegrationTest::assertJaegerHasATrace);
  }

  private static OpenTelemetry initOpenTelemetry() {
    Integer mappedPort = jaegerContainer.getMappedPort(THRIFT_HTTP_PORT);

    SpanExporter jaegerExporter =
        JaegerThriftSpanExporter.builder()
            .setEndpoint(JAEGER_URL + ":" + mappedPort + "/api/traces")
            .build();
    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                .setResource(
                    Resource.getDefault().toBuilder()
                        .put(ResourceAttributes.SERVICE_NAME, SERVICE_NAME)
                        .build())
                .build())
        .build();
  }

  private void imitateWork(OpenTelemetry openTelemetry) {
    Span span =
        openTelemetry.getTracer(getClass().getCanonicalName()).spanBuilder("Test span").startSpan();
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
