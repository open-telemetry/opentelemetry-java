/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.io.ByteStreams;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class XraySamplerClientTest {

  @RegisterExtension
  public static SelfSignedCertificateExtension certificate = new SelfSignedCertificateExtension();

  @RegisterExtension
  public static ServerExtension server =
      new ServerExtension() {

        @Override
        protected void configure(ServerBuilder sb) throws Exception {
          byte[] getSamplingRulesResponse =
              ByteStreams.toByteArray(
                  requireNonNull(
                      XraySamplerClientTest.class.getResourceAsStream(
                          "/get-sampling-rules-response.json")));

          sb.decorator(LoggingService.newDecorator());
          sb.service(
              "/GetSamplingRules",
              (ctx, req) ->
                  HttpResponse.from(
                      req.aggregate()
                          .thenApply(
                              aggregatedReq -> {
                                assertThat(aggregatedReq.contentUtf8())
                                    .isEqualTo("{\"NextToken\":\"token\"}");
                                return HttpResponse.of(
                                    HttpStatus.OK, MediaType.JSON_UTF_8, getSamplingRulesResponse);
                              })));
        }
      };

  private XraySamplerClient client;

  @BeforeEach
  void setUp() {
    client = new XraySamplerClient(server.httpUri().toString());
  }

  @Test
  void getSamplingRules() {
    GetSamplingRulesResponse response =
        client.getSamplingRules(GetSamplingRulesRequest.create("token"));

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
}
