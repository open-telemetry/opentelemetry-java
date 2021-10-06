/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.time.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Integration test to verify that the Jaeger GRPC exporter works. */
@Testcontainers(disabledWithoutDocker = true)
class JaegerExporterIntegrationTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final OkHttpClient client = new OkHttpClient();

  private static final int QUERY_PORT = 16686;
  private static final int JAEGER_API_PORT = 14250;
  private static final int HEALTH_PORT = 14269;
  private static final String SERVICE_NAME = "integration test";
  private static final String JAEGER_URL = "http://localhost";

  @Container
  public static GenericContainer<?> jaegerContainer =
      new GenericContainer<>(
              DockerImageName.parse("ghcr.io/open-telemetry/opentelemetry-java/jaeger"))
          .withExposedPorts(JAEGER_API_PORT, QUERY_PORT, HEALTH_PORT)
          .waitingFor(Wait.forHttp("/").forPort(HEALTH_PORT));

  @Test
  void testJaegerExampleAppIntegration() {
    OpenTelemetry openTelemetry =
        initOpenTelemetry(
            jaegerContainer.getHost(), jaegerContainer.getMappedPort(JAEGER_API_PORT));
    myWonderfulUseCase(openTelemetry);

    Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .until(JaegerExporterIntegrationTest::assertJaegerHasTheTrace);
  }

  private static Boolean assertJaegerHasTheTrace() {
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

  private static OpenTelemetry initOpenTelemetry(String ip, int port) {
    // Create a channel for the Jaeger endpoint
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
    // Export traces to Jaeger
    JaegerGrpcSpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.builder()
            .setChannel(jaegerChannel)
            .setTimeout(Duration.ofSeconds(30))
            .build();

    // Set to process the spans by the Jaeger Exporter
    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                .setResource(
                    Resource.getDefault().toBuilder()
                        .put(ResourceAttributes.SERVICE_NAME, "integration test")
                        .build())
                .build())
        .buildAndRegisterGlobal();
  }

  private static void myWonderfulUseCase(OpenTelemetry openTelemetry) {
    // Generate a span
    Span span =
        openTelemetry
            .getTracer("io.opentelemetry.SendTraceToJaeger")
            .spanBuilder("Start my wonderful use case")
            .startSpan();
    span.addEvent("Event 0");
    // execute my use case - here we simulate a wait
    doWait();
    span.addEvent("Event 1");
    span.end();
  }

  private static void doWait() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // catch
    }
  }
}
