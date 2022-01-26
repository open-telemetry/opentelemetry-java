/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.retry;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.internal.grpc.DefaultGrpcExporterBuilder;
import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporterBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class RetryUtilTest {

  @Test
  void setRetryPolicyOnDelegate_DefaultGrpcExporterBuilder() throws URISyntaxException {
    RetryPolicy retryPolicy = RetryPolicy.getDefault();
    DefaultGrpcExporterBuilder<?> builder =
        new DefaultGrpcExporterBuilder<>(
            "test", unused -> null, 0, new URI("http://localhost"), "test");

    RetryUtil.setRetryPolicyOnDelegate(new WithDelegate(builder), retryPolicy);

    assertThat(builder)
        .extracting("retryPolicy", as(InstanceOfAssertFactories.type(RetryPolicy.class)))
        .isEqualTo(retryPolicy);
  }

  @Test
  void setRetryPolicyOnDelegate_OkHttpGrpcExporterBuilder() throws URISyntaxException {
    RetryPolicy retryPolicy = RetryPolicy.getDefault();
    OkHttpGrpcExporterBuilder<?> builder =
        new OkHttpGrpcExporterBuilder<>("test", "/test", 0, new URI("http://localhost"));

    RetryUtil.setRetryPolicyOnDelegate(new WithDelegate(builder), retryPolicy);

    assertThat(builder)
        .extracting("retryPolicy", as(InstanceOfAssertFactories.type(RetryPolicy.class)))
        .isEqualTo(retryPolicy);
  }

  @Test
  void setRetryPolicyOnDelegate_InvalidUsage() {
    assertThatThrownBy(
            () -> RetryUtil.setRetryPolicyOnDelegate(new Object(), RetryPolicy.getDefault()))
        .hasMessageContaining("Unable to access delegate reflectively");
    assertThatThrownBy(
            () ->
                RetryUtil.setRetryPolicyOnDelegate(
                    new WithDelegate(new Object()), RetryPolicy.getDefault()))
        .hasMessageContaining(
            "delegate field is not type DefaultGrpcExporterBuilder or OkHttpGrpcExporterBuilder");
  }

  @SuppressWarnings({"UnusedVariable", "FieldCanBeLocal"})
  private static class WithDelegate {
    private final Object delegate;

    private WithDelegate(Object delegate) {
      this.delegate = delegate;
    }
  }
}
