/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RetryPolicyTest {

  @Test
  void defaultRetryPolicy() {
    assertThat(RetryPolicy.getDefault().getMaxAttempts()).isEqualTo(5);
    assertThat(RetryPolicy.getDefault().getInitialBackoff()).isEqualTo(Duration.ofSeconds(1));
    assertThat(RetryPolicy.getDefault().getMaxBackoff()).isEqualTo(Duration.ofSeconds(5));
    assertThat(RetryPolicy.getDefault().getBackoffMultiplier()).isEqualTo(1.5);
  }

  @Test
  void build() {
    RetryPolicy retryPolicy =
        RetryPolicy.builder()
            .setMaxAttempts(2)
            .setInitialBackoff(Duration.ofMillis(2))
            .setMaxBackoff(Duration.ofSeconds(1))
            .setBackoffMultiplier(1.1)
            .build();
    assertThat(retryPolicy.getMaxAttempts()).isEqualTo(2);
    assertThat(retryPolicy.getInitialBackoff()).isEqualTo(Duration.ofMillis(2));
    assertThat(retryPolicy.getMaxBackoff()).isEqualTo(Duration.ofSeconds(1));
    assertThat(retryPolicy.getBackoffMultiplier()).isEqualTo(1.1);
  }

  @Test
  void invalidRetryPolicy() {
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxAttempts(1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxAttempts(6))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setInitialBackoff(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setInitialBackoff(Duration.ofMillis(0)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxBackoff(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxBackoff(Duration.ofMillis(0)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setBackoffMultiplier(0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
