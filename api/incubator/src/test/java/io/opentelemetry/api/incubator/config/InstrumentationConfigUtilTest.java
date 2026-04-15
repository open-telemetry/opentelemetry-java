/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.YamlDeclarativeConfigProperties;
import io.opentelemetry.sdk.internal.SdkConfigProvider;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class InstrumentationConfigUtilTest {

  /**
   * See <a
   * href="https://github.com/open-telemetry/opentelemetry-configuration/blob/main/examples/kitchen-sink.yaml">kitchen-sink.yaml</a>.
   */
  private static final String kitchenSinkInstrumentationConfig =
      "instrumentation/development:\n"
          + "  general:\n"
          + "    peer:\n"
          + "      service_mapping:\n"
          + "        - peer: 1.2.3.4\n"
          + "          service: FooService\n"
          + "        - peer: 2.3.4.5\n"
          + "          service: BarService\n"
          + "    http:\n"
          + "      client:\n"
          + "        request_captured_headers:\n"
          + "          - client-request-header1\n"
          + "          - client-request-header2\n"
          + "        response_captured_headers:\n"
          + "          - client-response-header1\n"
          + "          - client-response-header2\n"
          + "      server:\n"
          + "        request_captured_headers:\n"
          + "          - server-request-header1\n"
          + "          - server-request-header2\n"
          + "        response_captured_headers:\n"
          + "          - server-response-header1\n"
          + "          - server-response-header2\n"
          + "  java:\n"
          + "    example:\n"
          + "      property: \"value\"";

  private static final ConfigProvider kitchenSinkConfigProvider =
      toConfigProvider(kitchenSinkInstrumentationConfig);
  private static final ConfigProvider emptyInstrumentationConfigProvider =
      toConfigProvider("instrumentation/development:\n");
  private static final ConfigProvider emptyGeneralConfigProvider =
      toConfigProvider("instrumentation/development:\n  general:\n");
  private static final ConfigProvider emptyHttpConfigProvider =
      toConfigProvider("instrumentation/development:\n  general:\n    http:\n");

  private static ConfigProvider toConfigProvider(String configYaml) {
    return SdkConfigProvider.create(
        DeclarativeConfiguration.toConfigProperties(
            new ByteArrayInputStream(configYaml.getBytes(StandardCharsets.UTF_8))));
  }

  @SuppressWarnings("deprecation") // testing deprecated method
  @Test
  void peerServiceMapping() {
    assertThat(InstrumentationConfigUtil.peerServiceMapping(kitchenSinkConfigProvider))
        .isEqualTo(ImmutableMap.of("1.2.3.4", "FooService", "2.3.4.5", "BarService"));
    assertThat(InstrumentationConfigUtil.peerServiceMapping(emptyInstrumentationConfigProvider))
        .isNull();
    assertThat(InstrumentationConfigUtil.peerServiceMapping(emptyGeneralConfigProvider)).isNull();
    assertThat(InstrumentationConfigUtil.peerServiceMapping(emptyHttpConfigProvider)).isNull();
  }

  @Test
  void httpClientRequestCapturedHeaders() {
    assertThat(
            InstrumentationConfigUtil.httpClientRequestCapturedHeaders(kitchenSinkConfigProvider))
        .isEqualTo(Arrays.asList("client-request-header1", "client-request-header2"));
    assertThat(
            InstrumentationConfigUtil.httpClientRequestCapturedHeaders(
                emptyInstrumentationConfigProvider))
        .isNull();
    assertThat(
            InstrumentationConfigUtil.httpClientRequestCapturedHeaders(emptyGeneralConfigProvider))
        .isNull();
    assertThat(InstrumentationConfigUtil.httpClientRequestCapturedHeaders(emptyHttpConfigProvider))
        .isNull();
  }

  @Test
  void httpClientResponseCapturedHeaders() {
    assertThat(
            InstrumentationConfigUtil.httpClientResponseCapturedHeaders(kitchenSinkConfigProvider))
        .isEqualTo(Arrays.asList("client-response-header1", "client-response-header2"));
    assertThat(
            InstrumentationConfigUtil.httpClientResponseCapturedHeaders(
                emptyInstrumentationConfigProvider))
        .isNull();
    assertThat(
            InstrumentationConfigUtil.httpClientResponseCapturedHeaders(emptyGeneralConfigProvider))
        .isNull();
    assertThat(InstrumentationConfigUtil.httpClientResponseCapturedHeaders(emptyHttpConfigProvider))
        .isNull();
  }

  @Test
  void httpServerRequestCapturedHeaders() {
    assertThat(
            InstrumentationConfigUtil.httpServerRequestCapturedHeaders(kitchenSinkConfigProvider))
        .isEqualTo(Arrays.asList("server-request-header1", "server-request-header2"));
    assertThat(
            InstrumentationConfigUtil.httpServerRequestCapturedHeaders(
                emptyInstrumentationConfigProvider))
        .isNull();
    assertThat(
            InstrumentationConfigUtil.httpServerRequestCapturedHeaders(emptyGeneralConfigProvider))
        .isNull();
    assertThat(InstrumentationConfigUtil.httpServerRequestCapturedHeaders(emptyHttpConfigProvider))
        .isNull();
  }

  @Test
  void httpServerResponseCapturedHeaders() {
    assertThat(
            InstrumentationConfigUtil.httpServerResponseCapturedHeaders(kitchenSinkConfigProvider))
        .isEqualTo(Arrays.asList("server-response-header1", "server-response-header2"));
    assertThat(
            InstrumentationConfigUtil.httpServerResponseCapturedHeaders(
                emptyInstrumentationConfigProvider))
        .isNull();
    assertThat(
            InstrumentationConfigUtil.httpServerResponseCapturedHeaders(emptyGeneralConfigProvider))
        .isNull();
    assertThat(InstrumentationConfigUtil.httpServerResponseCapturedHeaders(emptyHttpConfigProvider))
        .isNull();
  }

  @Test
  void javaInstrumentationConfig() {
    assertThat(
            InstrumentationConfigUtil.javaInstrumentationConfig(
                kitchenSinkConfigProvider, "example"))
        .isNotNull()
        .isInstanceOfSatisfying(
            YamlDeclarativeConfigProperties.class,
            exampleConfig ->
                assertThat(DeclarativeConfigProperties.toMap(exampleConfig))
                    .isEqualTo(ImmutableMap.of("property", "value")));
    assertThat(
            InstrumentationConfigUtil.javaInstrumentationConfig(kitchenSinkConfigProvider, "foo"))
        .isNull();
    assertThat(
            InstrumentationConfigUtil.javaInstrumentationConfig(
                emptyInstrumentationConfigProvider, "example"))
        .isNull();
    assertThat(
            InstrumentationConfigUtil.javaInstrumentationConfig(
                emptyGeneralConfigProvider, "example"))
        .isNull();
    assertThat(
            InstrumentationConfigUtil.javaInstrumentationConfig(emptyHttpConfigProvider, "example"))
        .isNull();
  }
}
