/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.testcontainers.Testcontainers.exposeHostPorts;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsString;
import com.google.common.io.Resources;
import com.linecorp.armeria.client.WebClient;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractPrometheusIntegrationTest {

  protected SdkMeterProvider meterProvider;
  protected int port;

  private GenericContainer<?> prometheus;

  @BeforeAll
  void setUp(@TempDir Path tempDir) throws Exception {
    exposeHostPorts(port);

    String promConfigTemplate =
        Resources.toString(Resources.getResource("prom.yml"), StandardCharsets.UTF_8);
    Path promConfig = tempDir.resolve("prom.yml");
    Files.write(
        promConfig,
        promConfigTemplate
            .replace("{{APP_PORT}}", String.valueOf(port))
            .getBytes(StandardCharsets.UTF_8));

    prometheus =
        new GenericContainer<>("prom/prometheus:v2.30.3")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("prometheus")))
            .withExposedPorts(9090)
            .waitingFor(Wait.forHttp("/-/ready"))
            .withCopyFileToContainer(MountableFile.forHostPath(promConfig), "/etc/prom.yml")
            .withCommand("--config.file", "/etc/prom.yml");
    prometheus.start();
  }

  @AfterAll
  void tearDown() {
    prometheus.stop();
    meterProvider.shutdown();
  }

  @Test
  void endToEnd() {
    Meter meter = meterProvider.meterBuilder("test").build();

    meter
        .counterBuilder("requests")
        .build()
        .add(3, Attributes.builder().put("animal", "bear").build());
    meter
        .gaugeBuilder("lives")
        .buildWithCallback(
            result -> result.record(9, Attributes.builder().put("animal", "cat").build()));

    WebClient promClient = WebClient.of("http://localhost:" + prometheus.getMappedPort(9090));
    JSON json = JSON.builder().treeCodec(new JacksonJrsTreeCodec()).build();
    await()
        .untilAsserted(
            () -> {
              TreeNode response =
                  json.treeFrom(
                      promClient
                          .get("/api/v1/query?query=requests_total")
                          .aggregate()
                          .join()
                          .contentUtf8());
              assertThat(response.get("status"))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("success"));
              TreeNode result = response.get("data").get("result").get(0);
              assertThat(result).isNotNull();
              assertThat(result.get("metric").get("__name__"))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("requests_total"));
              assertThat(result.get("metric").get("animal"))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("bear"));
              assertThat(result.get("value").get(1))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("3"));

              response =
                  json.treeFrom(
                      promClient.get("/api/v1/query?query=lives").aggregate().join().contentUtf8());
              assertThat(response.get("status"))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("success"));
              result = response.get("data").get("result").get(0);
              assertThat(result).isNotNull();
              assertThat(result.get("metric").get("__name__"))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("lives"));
              assertThat(result.get("metric").get("animal"))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("cat"));
              assertThat(result.get("value").get(1))
                  .isInstanceOfSatisfying(
                      JrsString.class, s -> assertThat(s.getValue()).isEqualTo("9"));
            });
  }

  static class PrometheusHttpServerIntegrationTest extends AbstractPrometheusIntegrationTest {
    PrometheusHttpServerIntegrationTest() {
      PrometheusHttpServerFactory factory =
          (PrometheusHttpServerFactory)
              PrometheusHttpServer.builder().setPort(0).newMetricReaderFactory();
      meterProvider = SdkMeterProvider.builder().registerMetricReader(factory).build();
      port = factory.getAddress().getPort();
    }
  }

  static class PrometheusCollectorIntegrationTest extends AbstractPrometheusIntegrationTest {
    private final HTTPServer server;

    // Tests deprecated class
    @SuppressWarnings("deprecation")
    PrometheusCollectorIntegrationTest() throws IOException {
      server = new HTTPServer(0);
      port = server.getPort();

      meterProvider =
          SdkMeterProvider.builder().registerMetricReader(PrometheusCollector.create()).build();
    }

    @AfterAll
    void stopHttpServer() {
      server.close();
    }
  }
}
