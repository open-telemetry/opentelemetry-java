/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.jaeger;

import static io.restassured.RestAssured.given;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@EnabledIfSystemProperty(named = "enable.docker.tests", matches = "true")
class JaegerIntegrationTest {

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final String JAEGER_VERSION = "1.17";
  private static final String SERVICE_NAME = "E2E-test";
  private static final String JAEGER_URL = "http://localhost";
  private final Tracer tracer = OpenTelemetry.getTracer(getClass().getCanonicalName());

  @Container
  public static GenericContainer<?> jaegerContainer =
      new GenericContainer<>("jaegertracing/all-in-one:" + JAEGER_VERSION)
          .withExposedPorts(COLLECTOR_PORT, QUERY_PORT)
          .waitingFor(new HttpWaitStrategy().forPath("/"));

  @Test
  void testJaegerIntegration() {
    setupJaegerExporter();
    imitateWork();
    Awaitility.await()
        .atMost(30, TimeUnit.SECONDS)
        .until(JaegerIntegrationTest::assertJaegerHaveTrace);
  }

  private static void setupJaegerExporter() {
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress("127.0.0.1", jaegerContainer.getMappedPort(COLLECTOR_PORT))
            .usePlaintext()
            .build();
    SpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.newBuilder()
            .setServiceName(SERVICE_NAME)
            .setChannel(jaegerChannel)
            .setDeadlineMs(30000)
            .build();
    OpenTelemetrySdk.getTracerManagement()
        .addSpanProcessor(SimpleSpanProcessor.newBuilder(jaegerExporter).build());
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
      Response response =
          given()
              .headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON)
              .when()
              .get(url)
              .then()
              .contentType(ContentType.JSON)
              .extract()
              .response();
      Map<String, String> path = response.jsonPath().getMap("data[0]");
      return path.get("traceID") != null;
    } catch (Exception e) {
      return false;
    }
  }
}
