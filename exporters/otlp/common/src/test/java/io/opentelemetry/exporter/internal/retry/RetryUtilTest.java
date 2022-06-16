/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.retry;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class RetryUtilTest {

  @Test
  void setRetryPolicyOnDelegate_GrpcExporterBuilder() throws URISyntaxException {
    RetryPolicy retryPolicy = RetryPolicy.getDefault();
    GrpcExporterBuilder<?> builder =
        GrpcExporter.builder(
            "otlp", "test", 0, new URI("http://localhost"), () -> (u1, u2) -> null, "/test");

    RetryUtil.setRetryPolicyOnDelegate(new WithDelegate(builder), retryPolicy);

    assertThat(builder)
        .extracting("retryPolicy", as(InstanceOfAssertFactories.type(RetryPolicy.class)))
        .isEqualTo(retryPolicy);
  }

  @Test
  void setRetryPolicyOnDelegate_OkHttpExporterBuilder() {
    RetryPolicy retryPolicy = RetryPolicy.getDefault();
    OkHttpExporterBuilder<?> builder =
        new OkHttpExporterBuilder<>("otlp", "test", "http://localhost:4318/test");
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
