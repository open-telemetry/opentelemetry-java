/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
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
@Testcontainers(disabledWithoutDocker = true)
class JaegerExporterIntegrationTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final OkHttpClient client = new OkHttpClient();

  private static final String ARCHIVE_NAME = System.getProperty("archive.name");
  private static final String APP_NAME = "SendTraceToJaeger.jar";

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final String SERVICE_NAME = "integration test";
  private static final String JAEGER_HOSTNAME = "jaeger";
  private static final String JAEGER_URL = "http://localhost";

  private static final Network network = Network.SHARED;

  @SuppressWarnings("rawtypes")
  @Container
  public static GenericContainer jaegerContainer =
      new GenericContainer<>(
              DockerImageName.parse(
                  "open-telemetry-docker-dev.bintray.io/java-test-containers:jaeger"))
          .withNetwork(network)
          .withNetworkAliases(JAEGER_HOSTNAME)
          .withExposedPorts(COLLECTOR_PORT, QUERY_PORT)
          .waitingFor(
              new HttpWaitStrategy().forPath("/").withStartupTimeout(Duration.ofMinutes(2)));

  @SuppressWarnings("rawtypes")
  @Container
  public static GenericContainer jaegerExampleAppContainer =
      new GenericContainer(
              DockerImageName.parse(
                  "open-telemetry-docker-dev.bintray.io/java-test-containers:openjdk8"))
          .withNetwork(network)
          .withCopyFileToContainer(MountableFile.forHostPath(ARCHIVE_NAME), "/app/" + APP_NAME)
          .withCommand(
              "java",
              "-cp",
              "/app/" + APP_NAME,
              "io.opentelemetry.SendTraceToJaeger",
              JAEGER_HOSTNAME,
              Integer.toString(COLLECTOR_PORT))
          .waitingFor(Wait.forLogMessage(".*Bye.*", 1).withStartupTimeout(Duration.ofMinutes(2)))
          .dependsOn(jaegerContainer);

  @Test
  void testJaegerExampleAppIntegration() {
    Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .until(JaegerExporterIntegrationTest::assertJaegerHaveTrace);
  }

  private static Boolean assertJaegerHaveTrace() {
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
