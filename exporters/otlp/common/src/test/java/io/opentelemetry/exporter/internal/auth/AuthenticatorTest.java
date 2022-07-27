/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.auth;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class AuthenticatorTest {

  @Test
  void getHeaders() {
    Map<String, String> input = new HashMap<>();
    input.put("key1", "value1");
    input.put("key2", "value2");

    Authenticator authenticator = () -> new HashMap<>(input);
    assertThat(authenticator.getHeaders()).isEqualTo(input);
  }

  @Test
  void setAuthenticatorOnDelegate_Success() {
    OkHttpExporterBuilder<?> builder =
        new OkHttpExporterBuilder<>("otlp", "test", "http://localhost:4318/test");

    assertThat(builder).extracting("authenticator").isNull();

    Authenticator authenticator = Collections::emptyMap;

    Authenticator.setAuthenticatorOnDelegate(new WithDelegate(builder), authenticator);

    assertThat(builder)
        .extracting("authenticator", as(InstanceOfAssertFactories.type(Authenticator.class)))
        .isSameAs(authenticator);
  }

  @Test
  void setAuthenticatorOnDelegate_Fail() {
    Authenticator authenticator = Collections::emptyMap;

    assertThatThrownBy(() -> Authenticator.setAuthenticatorOnDelegate(new Object(), authenticator))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                Authenticator.setAuthenticatorOnDelegate(
                    new WithDelegate(new Object()), authenticator))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                Authenticator.setAuthenticatorOnDelegate(
                    new WithDelegate(GrpcExporter.builder(null, null, 0, null, null, null)),
                    authenticator))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @SuppressWarnings({"UnusedVariable", "FieldCanBeLocal"})
  private static class WithDelegate {

    private final Object delegate;

    private WithDelegate(Object delegate) {
      this.delegate = delegate;
    }
  }
}
