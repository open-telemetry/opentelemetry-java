/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import javax.annotation.Nullable;
import org.awaitility.Awaitility;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public class JaegerIntegrationTest {

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final String JAEGER_VERSION = "1.17";
  private static final String SERVICE_NAME = "E2E-test";
  private static final String JAEGER_URL = "http://localhost";
  private final Tracer tracer = OpenTelemetry.getTracer(getClass().getCanonicalName());

  @SuppressWarnings("rawtypes")
  @ClassRule
  @Nullable
  public static GenericContainer jaegerContainer = null;

  static {
    // make sure that the user has enabled the docker-based tests
    if (Boolean.getBoolean("enable.docker.tests")) {
      jaegerContainer =
          new GenericContainer<>("jaegertracing/all-in-one:" + JAEGER_VERSION)
              .withExposedPorts(COLLECTOR_PORT, QUERY_PORT)
              .waitingFor(new HttpWaitStrategy().forPath("/"));
    }
  }

  @Test
  public void testJaegerIntegration() {
    Assume.assumeNotNull(jaegerContainer);
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
    OpenTelemetrySdk.getTracerProvider()
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
