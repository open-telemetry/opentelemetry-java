/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EndpointUtilTest {

  @ParameterizedTest
  @MethodSource("validEndpoints")
  void validateEndpoint_valid(String endpoint) {
    assertThat(EndpointUtil.validateEndpoint(endpoint)).isEqualTo(URI.create(endpoint));
  }

  private static Stream<Arguments> validEndpoints() {
    return Stream.of(
        Arguments.argumentSet("http", "http://localhost:4318"),
        Arguments.argumentSet("https", "https://localhost:4317"),
        Arguments.argumentSet("path", "http://localhost:4318/v1/traces"),
        Arguments.argumentSet("userinfo", "http://foo:bar@localhost:4317/path"));
  }

  @ParameterizedTest
  @MethodSource("invalidEndpoints")
  void validateEndpoint_invalid(String endpoint) {
    assertThatThrownBy(() -> EndpointUtil.validateEndpoint(endpoint))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must start with http:// or https://");
  }

  private static Stream<Arguments> invalidEndpoints() {
    return Stream.of(
        Arguments.argumentSet("opaque, no host", "http:localhost:4317"),
        Arguments.argumentSet("single slash, no host", "https:/foo"),
        Arguments.argumentSet("no scheme", "localhost"),
        Arguments.argumentSet("wrong scheme", "gopher://localhost"));
  }
}
