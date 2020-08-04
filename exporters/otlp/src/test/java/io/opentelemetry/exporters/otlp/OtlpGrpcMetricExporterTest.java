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

package io.opentelemetry.exporters.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.opentelemetry.common.Labels;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.export.MetricExporter.ResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

/** Unit tests for {@link OtlpGrpcMetricExporter}. */
public class OtlpGrpcMetricExporterTest {
  @Rule public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private final FakeCollector fakeCollector = new FakeCollector();
  private final String serverName = InProcessServerBuilder.generateName();
  private final ManagedChannel inProcessChannel =
      InProcessChannelBuilder.forName(serverName).directExecutor().build();

  abstract static class ConfigBuilderTest extends ConfigBuilder<ConfigBuilderTest> {
    public static NamingConvention getNaming() {
      return NamingConvention.DOT;
    }
  }

  @Before
  public void setup() throws IOException {
    grpcCleanup.register(
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(fakeCollector)
            .build()
            .start());
    grpcCleanup.register(inProcessChannel);
  }

  @Test
  public void configTest() {
    Map<String, String> options = new HashMap<>();
    options.put("otel.otlp.metric.timeout", "12");
    OtlpGrpcMetricExporter.Builder config = OtlpGrpcMetricExporter.newBuilder();
    OtlpGrpcMetricExporter.Builder spy = Mockito.spy(config);
    spy.fromConfigMap(options, ConfigBuilderTest.getNaming());
    Mockito.verify(spy).setDeadlineMs(12);
  }

  @Test
  public void testExport() {
    MetricData span = generateFakeMetric();
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(span))).isEqualTo(ResultCode.SUCCESS);
      assertThat(fakeCollector.getReceivedMetrics())
          .isEqualTo(MetricAdapter.toProtoResourceMetrics(Collections.singletonList(span)));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_MultipleMetrics() {
    List<MetricData> spans = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      spans.add(generateFakeMetric());
    }
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(spans)).isEqualTo(ResultCode.SUCCESS);
      assertThat(fakeCollector.getReceivedMetrics())
          .isEqualTo(MetricAdapter.toProtoResourceMetrics(spans));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_AfterShutdown() {
    MetricData span = generateFakeMetric();
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    exporter.shutdown();
    assertThat(exporter.export(Collections.singletonList(span))).isEqualTo(ResultCode.FAILURE);
  }

  @Test
  public void testExport_Cancelled() {
    fakeCollector.setReturnedStatus(Status.CANCELLED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())))
          .isEqualTo(ResultCode.FAILURE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_DeadlineExceeded() {
    fakeCollector.setReturnedStatus(Status.DEADLINE_EXCEEDED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())))
          .isEqualTo(ResultCode.FAILURE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_ResourceExhausted() {
    fakeCollector.setReturnedStatus(Status.RESOURCE_EXHAUSTED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())))
          .isEqualTo(ResultCode.FAILURE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_OutOfRange() {
    fakeCollector.setReturnedStatus(Status.OUT_OF_RANGE);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())))
          .isEqualTo(ResultCode.FAILURE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_Unavailable() {
    fakeCollector.setReturnedStatus(Status.UNAVAILABLE);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())))
          .isEqualTo(ResultCode.FAILURE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_DataLoss() {
    fakeCollector.setReturnedStatus(Status.DATA_LOSS);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())))
          .isEqualTo(ResultCode.FAILURE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_PermissionDenied() {
    fakeCollector.setReturnedStatus(Status.PERMISSION_DENIED);
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.export(Collections.singletonList(generateFakeMetric())))
          .isEqualTo(ResultCode.FAILURE);
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  public void testExport_flush() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.newBuilder().setChannel(inProcessChannel).build();
    try {
      assertThat(exporter.flush()).isEqualTo(ResultCode.SUCCESS);
    } finally {
      exporter.shutdown();
    }
  }

  private static MetricData generateFakeMetric() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricData.create(
        Descriptor.create(
            "name", "description", "1", Descriptor.Type.MONOTONIC_LONG, Labels.empty()),
        Resource.getEmpty(),
        InstrumentationLibraryInfo.getEmpty(),
        Collections.singletonList(LongPoint.create(startNs, endNs, Labels.of("k", "v"), 5)));
  }

  private static final class FakeCollector extends MetricsServiceGrpc.MetricsServiceImplBase {
    private final List<ResourceMetrics> receivedMetrics = new ArrayList<>();
    private Status returnedStatus = Status.OK;

    @Override
    public void export(
        ExportMetricsServiceRequest request,
        StreamObserver<ExportMetricsServiceResponse> responseObserver) {
      receivedMetrics.addAll(request.getResourceMetricsList());
      responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().build());
      if (!returnedStatus.isOk()) {
        if (returnedStatus.getCode() == Code.DEADLINE_EXCEEDED) {
          // Do not call onCompleted to simulate a deadline exceeded.
          return;
        }
        responseObserver.onError(returnedStatus.asRuntimeException());
        return;
      }
      responseObserver.onCompleted();
    }

    List<ResourceMetrics> getReceivedMetrics() {
      return receivedMetrics;
    }

    void setReturnedStatus(Status returnedStatus) {
      this.returnedStatus = returnedStatus;
    }
  }
}
