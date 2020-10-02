/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Integration test to verify that OpenTelemetry artefacts run in JRE 7.
 *
 * <p>An executable JAR with dependencies containing {@link SendTraceToJaeger} is built prior to
 * executing the tests.
 *
 * <p>A Jaeger-all-in-one container is started, then an Alpine JRE 7 container is started with the
 * executable JAR added to it and executed which will send a trace to the Jaeger instance. The test
 * verifies that the trace is received by Jaeger.
 */
@Testcontainers
class JaegerExporterIntegrationTest {

  private static final String ARCHIVE_NAME = System.getProperty("archive.name");
  private static final String APP_NAME = "SendTraceToJaeger.jar";

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final String JAEGER_VERSION = "1.17";
  private static final String SERVICE_NAME = "integration test";
  private static final String JAEGER_HOSTNAME = "jaeger";
  private static final String JAEGER_URL = "http://localhost";

  private static final Network network = Network.SHARED;

  @SuppressWarnings("rawtypes")
  @Container
  public static GenericContainer jaegerContainer =
      new GenericContainer<>("jaegertracing/all-in-one:" + JAEGER_VERSION)
          .withNetwork(network)
          .withNetworkAliases(JAEGER_HOSTNAME)
          .withExposedPorts(COLLECTOR_PORT, QUERY_PORT)
          .waitingFor(new HttpWaitStrategy().forPath("/"));

  @SuppressWarnings("rawtypes")
  @Container
  public static GenericContainer jaegerExampleAppContainer =
      new GenericContainer("adoptopenjdk/openjdk8")
          .withNetwork(network)
          .withCopyFileToContainer(MountableFile.forHostPath(ARCHIVE_NAME), "/app/" + APP_NAME)
          .withCommand(
              "java",
              "-cp",
              "/app/" + APP_NAME,
              "io.opentelemetry.SendTraceToJaeger",
              JAEGER_HOSTNAME,
              Integer.toString(COLLECTOR_PORT))
          .waitingFor(Wait.forLogMessage(".*Bye.*", 1))
          .dependsOn(jaegerContainer);

  @Test
  void testJaegerExampleAppIntegration() {
    Awaitility.await()
        .atMost(30, TimeUnit.SECONDS)
        .until(JaegerExporterIntegrationTest::assertJaegerHaveTrace);
  }

  private static Boolean assertJaegerHaveTrace() {
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
