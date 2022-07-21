/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.auth;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

/** Authenticator tests. */
class AuthenticatorTest {

  @Test
  void testGetHeaders() {
    Map<String, String> input = new HashMap<>();
    input.put("key1", "value1");
    input.put("key2", "value2");
    Map<String, String> result = new HashMap<>();

    Authenticator authenticator =
        (Consumer<Map<String, String>> headers) -> {
          headers.accept(input);
        };
    authenticator.getHeaders(result::putAll);
    assertEquals(input, result);
  }

  @Test
  void testSetAuthenticatorOnDelegate_Success() {
    OkHttpExporterBuilder<?> builder =
        new OkHttpExporterBuilder<>("otlp", "test", "http://localhost:4318/test");

    assertThat(builder).extracting("authenticator").isNull();

    Authenticator authenticator = (Consumer<Map<String, String>> headers) -> {};

    Authenticator.setAuthenticatorOnDelegate(new WithDelegate(builder), authenticator);

    assertThat(builder)
        .extracting("authenticator", as(InstanceOfAssertFactories.type(Authenticator.class)))
        .isSameAs(authenticator);
  }

  @Test
  void testSetAuthenticatorOnDelegate_Fail() {

    Authenticator authenticator = (Consumer<Map<String, String>> headers) -> {};

    assertThatThrownBy(() -> Authenticator.setAuthenticatorOnDelegate(new Object(), authenticator))
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
