/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;

class XraySamplerClientTest {

  @RegisterExtension public static MockWebServerExtension server = new MockWebServerExtension();

  private XraySamplerClient client;

  @BeforeEach
  void setUp() {
    client = new XraySamplerClient(server.httpUri().toString());
  }

  @Test
  void getSamplingRules() throws Exception {
    enqueueResource("/get-sampling-rules-response.json");
    GetSamplingRulesResponse response =
        client.getSamplingRules(GetSamplingRulesRequest.create("token"));

    AggregatedHttpRequest request = server.takeRequest().request();
    assertThat(request.path()).isEqualTo("/GetSamplingRules");
    assertThat(request.contentType()).isEqualTo(MediaType.JSON);
    assertThat(request.contentUtf8()).isEqualTo("{\"NextToken\":\"token\"}");

    assertThat(response.getNextToken()).isNull();
    assertThat(response.getSamplingRules())
        .satisfiesExactly(
            rule -> {
              assertThat(rule.getCreatedAt()).isEqualTo("2021-06-18T17:28:15+09:00");
              assertThat(rule.getModifiedAt()).isEqualTo("2021-06-18T17:28:15+09:00");
              assertThat(rule.getRule().getRuleName()).isEqualTo("Test");
              assertThat(rule.getRule().getRuleArn())
                  .isEqualTo("arn:aws:xray:us-east-1:595986152929:sampling-rule/Test");
              assertThat(rule.getRule().getResourceArn()).isEqualTo("*");
              assertThat(rule.getRule().getPriority()).isEqualTo(1);
              assertThat(rule.getRule().getFixedRate()).isEqualTo(0.9);
              assertThat(rule.getRule().getReservoirSize()).isEqualTo(1000);
              assertThat(rule.getRule().getServiceName()).isEqualTo("test-service-foo-bar");
              assertThat(rule.getRule().getServiceType()).isEqualTo("*");
              assertThat(rule.getRule().getHost()).isEqualTo("*");
              assertThat(rule.getRule().getHttpMethod()).isEqualTo("*");
              assertThat(rule.getRule().getUrlPath()).isEqualTo("*");
              assertThat(rule.getRule().getVersion()).isEqualTo(1);
              assertThat(rule.getRule().getAttributes())
                  .containsExactly(entry("animal", "cat"), entry("speed", "10"));
            },
            rule -> {
              assertThat(rule.getCreatedAt()).isEqualTo("1970-01-01T09:00:00+09:00");
              assertThat(rule.getModifiedAt()).isEqualTo("1970-01-01T09:00:00+09:00");
              assertThat(rule.getRule().getRuleName()).isEqualTo("Default");
              assertThat(rule.getRule().getRuleArn())
                  .isEqualTo("arn:aws:xray:us-east-1:595986152929:sampling-rule/Default");
              assertThat(rule.getRule().getResourceArn()).isEqualTo("*");
              assertThat(rule.getRule().getPriority()).isEqualTo(10000);
              assertThat(rule.getRule().getFixedRate()).isEqualTo(0.05);
              assertThat(rule.getRule().getReservoirSize()).isEqualTo(1);
              assertThat(rule.getRule().getServiceName()).isEqualTo("*");
              assertThat(rule.getRule().getServiceType()).isEqualTo("*");
              assertThat(rule.getRule().getHost()).isEqualTo("*");
              assertThat(rule.getRule().getHttpMethod()).isEqualTo("*");
              assertThat(rule.getRule().getUrlPath()).isEqualTo("*");
              assertThat(rule.getRule().getVersion()).isEqualTo(1);
              assertThat(rule.getRule().getAttributes()).isEmpty();
            });
  }

  @Test
  void getSamplingRules_malformed() {
    server.enqueue(HttpResponse.of(HttpStatus.OK, MediaType.JSON, "notjson"));
    assertThatThrownBy(() -> client.getSamplingRules(GetSamplingRulesRequest.create("token")))
        .isInstanceOf(UncheckedIOException.class)
        .hasMessage("Failed to deserialize response.");
  }

  @Test
  void getSamplingTargets() throws Exception {
    // Request and response adapted from
    // https://docs.aws.amazon.com/xray/latest/devguide/xray-api-sampling.html
    enqueueResource("/get-sampling-targets-response.json");
    Date timestamp = Date.from(Instant.parse("2021-06-21T06:46:07Z"));
    Date timestamp2 = Date.from(Instant.parse("2018-07-07T00:20:06Z"));
    GetSamplingTargetsRequest samplingTargetsRequest =
        GetSamplingTargetsRequest.create(
            Arrays.asList(
                GetSamplingTargetsRequest.SamplingStatisticsDocument.newBuilder()
                    .setRuleName("Test")
                    .setClientId("ABCDEF1234567890ABCDEF10")
                    .setTimestamp(timestamp)
                    .setRequestCount(110)
                    .setSampledCount(30)
                    .setBorrowCount(20)
                    .build(),
                GetSamplingTargetsRequest.SamplingStatisticsDocument.newBuilder()
                    .setRuleName("polling-scorekeep")
                    .setClientId("ABCDEF1234567890ABCDEF11")
                    .setTimestamp(timestamp2)
                    .setRequestCount(10500)
                    .setSampledCount(31)
                    .setBorrowCount(0)
                    .build()));
    GetSamplingTargetsResponse response = client.getSamplingTargets(samplingTargetsRequest);

    AggregatedHttpRequest request = server.takeRequest().request();

    assertThat(request.path()).isEqualTo("/SamplingTargets");
    assertThat(request.contentType()).isEqualTo(MediaType.JSON);
    JSONAssert.assertEquals(
        Resources.toString(
            XraySamplerClientTest.class.getResource("/get-sampling-targets-request.json"),
            StandardCharsets.UTF_8),
        request.contentUtf8(),
        true);

    assertThat(response.getLastRuleModification())
        .isEqualTo(Date.from(Instant.parse("2018-07-06T23:41:45Z")));
    assertThat(response.getDocuments())
        .satisfiesExactly(
            document -> {
              assertThat(document.getRuleName()).isEqualTo("base-scorekeep");
              assertThat(document.getFixedRate()).isEqualTo(0.1);
              assertThat(document.getReservoirQuota()).isEqualTo(2);
              assertThat(document.getReservoirQuotaTtl())
                  .isEqualTo(Date.from(Instant.parse("2018-07-07T00:25:07Z")));
              assertThat(document.getIntervalSecs()).isEqualTo(10);
            },
            document -> {
              assertThat(document.getRuleName()).isEqualTo("polling-scorekeep");
              assertThat(document.getFixedRate()).isEqualTo(0.003);
              assertThat(document.getReservoirQuota()).isZero();
              assertThat(document.getReservoirQuotaTtl()).isNull();
              assertThat(document.getIntervalSecs()).isZero();
            });
    assertThat(response.getUnprocessedStatistics())
        .satisfiesExactly(
            statistics -> {
              assertThat(statistics.getRuleName()).isEqualTo("cats-rule");
              assertThat(statistics.getErrorCode()).isEqualTo("400");
              assertThat(statistics.getMessage()).isEqualTo("Unknown rule");
            });
  }

  @Test
  void getSamplingTargets_malformed() {
    server.enqueue(HttpResponse.of(HttpStatus.OK, MediaType.JSON, "notjson"));
    assertThatThrownBy(
            () ->
                client.getSamplingTargets(
                    GetSamplingTargetsRequest.create(Collections.emptyList())))
        .isInstanceOf(UncheckedIOException.class)
        .hasMessage("Failed to deserialize response.");
  }

  private static void enqueueResource(String resourcePath) throws Exception {
    server.enqueue(
        HttpResponse.of(
            HttpStatus.OK,
            MediaType.JSON_UTF_8,
            ByteStreams.toByteArray(
                requireNonNull(XraySamplerClientTest.class.getResourceAsStream(resourcePath)))));
  }
}
