/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metric;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Message;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.json.JsonMetricAdapter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.tls.HeldCertificate;
import okio.Buffer;
import okio.GzipSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class OtlpHttpJsonMetricExporterTest {
  private static final MediaType APPLICATION_JSON =
      MediaType.create("application", "json").withCharset(UTF_8);

  private static final HeldCertificate HELD_CERTIFICATE;

  static {
    try {
      HELD_CERTIFICATE =
          new HeldCertificate.Builder()
              .commonName("localhost")
              .addSubjectAlternativeName(InetAddress.getByName("localhost").getCanonicalHostName())
              .build();
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Error building certificate.", e);
    }
  }

  @RegisterExtension
  static MockWebServerExtension server =
      new MockWebServerExtension() {
        @Override
        protected void configureServer(ServerBuilder sb) {
          sb.tls(HELD_CERTIFICATE.keyPair().getPrivate(), HELD_CERTIFICATE.certificate());
        }
      };

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpHttpJsonMetricExporter.class);

  private OtlpHttpJsonMetricExporterBuilder builder;

  @BeforeEach
  void setup() {
    builder =
        OtlpHttpJsonMetricExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort() + "/v1/metrics")
            .addHeader("foo", "bar")
            .setTrustedCertificates(
                HELD_CERTIFICATE.certificatePem().getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(
            () -> OtlpHttpJsonMetricExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpHttpJsonMetricExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported compression method. Supported compression methods include: gzip.");
  }

  @Test
  void testExportUncompressed() {
    server.enqueue(successResponse());
    OtlpHttpJsonMetricExporter exporter = builder.build();

    JSONObject payload = exportAndAssertResult(exporter, /* expectedResult= */ true);
    AggregatedHttpRequest request = server.takeRequest().request();
    assertRequestCommon(request);
    boolean containsExactly =
        compareJsonObject(parseRequestBody(request.content().array()), payload);
    assertThat(containsExactly).isTrue();
  }

  @Test
  void testExportGzipCompressed() {
    server.enqueue(successResponse());
    OtlpHttpJsonMetricExporter exporter = builder.setCompression("gzip").build();

    JSONObject payload = exportAndAssertResult(exporter, /* expectedResult= */ true);
    AggregatedHttpRequest request = server.takeRequest().request();
    assertRequestCommon(request);
    assertThat(request.headers().get("Content-Encoding")).isEqualTo("gzip");
    boolean containsExactly =
        compareJsonObject(parseRequestBody(gzipDecompress(request.content().array())), payload);
    assertThat(containsExactly).isTrue();
  }

  private static void assertRequestCommon(AggregatedHttpRequest request) {
    assertThat(request.method()).isEqualTo(HttpMethod.POST);
    assertThat(request.path()).isEqualTo("/v1/metrics");
    assertThat(request.headers().get("foo")).isEqualTo("bar");
    assertThat(request.headers().get("Content-Type")).isEqualTo(APPLICATION_JSON.toString());
  }

  private static JSONObject parseRequestBody(byte[] bytes) {
    try {
      return (JSONObject) JSONObject.parse(bytes);
    } catch (JSONException e) {
      throw new IllegalStateException("Unable to parse JSON request body.", e);
    }
  }

  private static byte[] gzipDecompress(byte[] bytes) {
    try {
      Buffer result = new Buffer();
      GzipSource source = new GzipSource(new Buffer().write(bytes));
      while (source.read(result, Integer.MAX_VALUE) != -1) {}
      return result.readByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to decompress payload.", e);
    }
  }

  @Test
  void testServerErrorParseError() {
    server.enqueue(
        HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, APPLICATION_JSON, "Server error!"));
    OtlpHttpJsonMetricExporter exporter = builder.build();

    exportAndAssertResult(exporter, /* expectedResult= */ false);
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () -> {
              LoggingEvent log =
                  logs.assertContains(
                      "Failed to export metrics. Server responded with HTTP status code 500. Error message: Unable to parse response body, HTTP status message:");
              assertThat(log.getLevel()).isEqualTo(Level.WARN);
            });
  }

  private static JSONObject exportAndAssertResult(
      OtlpHttpJsonMetricExporter otlpHttpJsonMetricExporter, boolean expectedResult) {
    List<MetricData> metrics = Collections.singletonList(generateFakeMetric());
    CompletableResultCode resultCode = otlpHttpJsonMetricExporter.export(metrics);
    resultCode.join(10, TimeUnit.SECONDS);
    assertThat(resultCode.isSuccess()).isEqualTo(expectedResult);
    JSONObject request = new JSONObject();
    request.put("resource_metrics", JsonMetricAdapter.toJsonResourceMetrics(metrics));
    return request;
  }

  private static HttpResponse successResponse() {
    JSONObject exportMetricsServiceResponse = new JSONObject();
    return buildResponse(HttpStatus.OK, exportMetricsServiceResponse);
  }

  private static <T extends Message> HttpResponse buildResponse(
      HttpStatus httpStatus, JSONObject message) {
    return HttpResponse.of(httpStatus, APPLICATION_JSON, message.toJSONString());
  }

  private static MetricData generateFakeMetric() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricData.createLongSum(
        Resource.empty(),
        InstrumentationLibraryInfo.empty(),
        "name",
        "description",
        "1",
        LongSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                LongPointData.create(startNs, endNs, Attributes.of(stringKey("k"), "v"), 5))));
  }

  private static Boolean compareJsonObject(JSONObject actualJson, JSONObject exceptedJson) {
    List<String> resultList = new LinkedList<>();
    for (Map.Entry<String, Object> actualEntry : actualJson.entrySet()) {
      for (Map.Entry<String, Object> exceptedEntry : exceptedJson.entrySet()) {
        if (actualEntry.getKey().equals(exceptedEntry.getKey())) {
          if (!actualEntry.getValue().toString().equals(exceptedEntry.getValue().toString())) {
            resultList.add(actualEntry.getKey());
          }
        }
      }
    }
    return resultList.size() <= 0;
  }
}
