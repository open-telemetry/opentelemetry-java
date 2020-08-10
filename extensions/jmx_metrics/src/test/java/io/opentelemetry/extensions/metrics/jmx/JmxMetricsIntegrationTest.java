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

package io.opentelemetry.extensions.metrics.jmx;

import static io.opentelemetry.proto.metrics.v1.MetricDescriptor.Type.SUMMARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.StringKeyValue;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.MetricDescriptor;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

@RunWith(JUnit4.class)
public class JmxMetricsIntegrationTest {

  private static final String JAR_PATH = System.getProperty("fat.jar.path");
  private static final String JAR_NAME = "OpenTelemetryJava.jar";
  private static final String SCRIPT_NAME = "script.groovy";
  private static final String SCRIPT_PATH =
      ClassLoader.getSystemClassLoader().getResource(SCRIPT_NAME).getPath();
  private static final String CONFIG_NAME = "test_config.json";
  private static final String CONFIG_PATH =
      ClassLoader.getSystemClassLoader().getResource(CONFIG_NAME).getPath();

  private static final Collector otelCollector = new Collector();

  private static final int JMX_PORT = 7199;
  private static final String CASSANDRA_HOSTNAME = "cassandra";
  private static final String CASSANDRA_DOCKERFILE =
      "FROM cassandra:3.11\n"
          + "ENV LOCAL_JMX=no\n"
          + "RUN echo 'cassandra cassandra' > /etc/cassandra/jmxremote.password &&\\ \n"
          + "chmod 0400 /etc/cassandra/jmxremote.password";

  private static final Network network = Network.SHARED;
  private static final Server collector =
      ServerBuilder.forPort(9080).addService(otelCollector).build();

  @SuppressWarnings("rawtypes")
  @ClassRule
  @Nullable
  public static GenericContainer cassandraContainer = null;

  @SuppressWarnings("rawtypes")
  @ClassRule
  @Nullable
  public static GenericContainer jmxExtensionAppContainer = null;

  static {
    if (Boolean.getBoolean("enable.docker.tests")) {
      cassandraContainer =
          new GenericContainer<>(
                  new ImageFromDockerfile().withFileFromString("Dockerfile", CASSANDRA_DOCKERFILE))
              .withNetwork(network)
              .withNetworkAliases(CASSANDRA_HOSTNAME)
              .withExposedPorts(JMX_PORT)
              .waitingFor(Wait.forListeningPort());

      jmxExtensionAppContainer =
          new GenericContainer("openjdk:7u111-jre-alpine")
              .withNetwork(network)
              .withCopyFileToContainer(MountableFile.forHostPath(JAR_PATH), "/app/" + JAR_NAME)
              .withCopyFileToContainer(
                  MountableFile.forHostPath(SCRIPT_PATH), "/app/" + SCRIPT_NAME)
              .withCopyFileToContainer(
                  MountableFile.forHostPath(CONFIG_PATH), "/app/" + CONFIG_NAME)
              .withCommand(
                  "java -cp /app/OpenTelemetryJava.jar "
                      + "io.opentelemetry.extensions.metrics.jmx.JmxMetrics "
                      + "-config /app/test_config.json")
              .waitingFor(Wait.forLogMessage(".*Started GroovyRunner.*", 1))
              .dependsOn(cassandraContainer);
    }
  }

  @ClassRule
  public static ExternalResource server =
      new ExternalResource() {
        @Override
        protected void before() throws Throwable {
          if (Boolean.getBoolean("enable.docker.tests")) {
            collector.start();
          }
        }

        @Override
        protected void after() {
          if (Boolean.getBoolean("enable.docker.tests")) {
            collector.shutdown();
          }
        }
      };

  @Test
  public void endToEnd() {
    Assume.assumeNotNull(cassandraContainer, jmxExtensionAppContainer);
    List<ResourceMetrics> receivedMetrics = otelCollector.getReceivedMetrics();
    assertEquals(1, receivedMetrics.size());
    ResourceMetrics receivedMetric = receivedMetrics.get(0);

    List<InstrumentationLibraryMetrics> ilMetrics =
        receivedMetric.getInstrumentationLibraryMetricsList();
    assertEquals(1, ilMetrics.size());
    InstrumentationLibraryMetrics ilMetric = ilMetrics.get(0);

    InstrumentationLibrary il = ilMetric.getInstrumentationLibrary();
    assertEquals("jmx-metrics", il.getName());
    assertEquals("0.0.1", il.getVersion());

    List<Metric> metrics = ilMetric.getMetricsList();
    assertEquals(1, metrics.size());

    Metric metric = metrics.get(0);
    MetricDescriptor md = metric.getMetricDescriptor();
    assertEquals("cassandra.storage.load", md.getName());
    assertEquals("Size, in bytes, of the on disk data size this node manages", md.getDescription());
    assertEquals("By", md.getUnit());
    assertEquals(SUMMARY, md.getType());

    List<SummaryDataPoint> datapoints = metric.getSummaryDataPointsList();
    assertEquals(1, datapoints.size());
    SummaryDataPoint datapoint = datapoints.get(0);

    List<StringKeyValue> labels = datapoint.getLabelsList();
    assertEquals(1, labels.size());
    assertEquals(
        StringKeyValue.newBuilder().setKey("myKey").setValue("myVal").build(), labels.get(0));

    assertEquals(1, datapoint.getCount());
    double sum = datapoint.getSum();
    assertEquals(sum, datapoint.getPercentileValues(0).getValue(), 0);
    assertEquals(sum, datapoint.getPercentileValues(1).getValue(), 0);
  }

  private static final class Collector extends MetricsServiceGrpc.MetricsServiceImplBase {
    private final List<ResourceMetrics> receivedMetrics = new ArrayList<>();
    private final Object monitor = new Object();

    @Override
    public void export(
        ExportMetricsServiceRequest request,
        StreamObserver<ExportMetricsServiceResponse> responseObserver) {
      synchronized (receivedMetrics) {
        receivedMetrics.addAll(request.getResourceMetricsList());
      }
      synchronized (monitor) {
        monitor.notify();
      }
      responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().build());
      responseObserver.onCompleted();
    }

    List<ResourceMetrics> getReceivedMetrics() {
      List<ResourceMetrics> received;
      try {
        synchronized (monitor) {
          monitor.wait(15000);
        }
      } catch (final InterruptedException e) {
        assertTrue(e.getMessage(), false);
      }

      synchronized (receivedMetrics) {
        received = new ArrayList<>(receivedMetrics);
        receivedMetrics.clear();
      }
      return received;
    }
  }
}
