/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtests.tracecontext;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers(disabledWithoutDocker = true)
class TraceContextIntegrationTest {

  @Container
  private static final GenericContainer<?> appContainer =
      new GenericContainer<>(
              DockerImageName.parse("ghcr.io/open-telemetry/java-test-containers:openjdk8"))
          .withExposedPorts(5000)
          .withNetwork(Network.SHARED)
          .withNetworkAliases("app")
          .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("app")))
          .withCommand("java", "-jar", "/opt/app.jar")
          .waitingFor(Wait.forHttp("/health"))
          .withCopyFileToContainer(
              MountableFile.forHostPath(System.getProperty("io.opentelemetry.testArchive")),
              "/opt/app.jar");

  @Container
  private static final GenericContainer<?> testSuiteContainer =
      new GenericContainer<>(
              DockerImageName.parse(
                  "ghcr.io/open-telemetry/java-test-containers:w3c-tracecontext-testsuite"))
          .withNetwork(Network.SHARED)
          .withNetworkAliases("testsuite")
          .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testsuite")))
          .withCommand("http://app:5000/verify-tracecontext")
          .withEnv("HARNESS_HOST", "testsuite")
          .waitingFor(Wait.forLogMessage(".*Ran \\d+ tests in \\d+\\.\\d+s.*", 1))
          .dependsOn(appContainer);

  @Test
  void run() {
    // TODO(anuraaga): We currently run all tests to print logs of our compatibility, including many
    // failing tests. If we are ever able to fix the tests we can add an assertion here that the
    // test succeeded.
  }
}
