/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class RetryPolicyTest {

  @Test
  void defaultRetryPolicy() {
    RetryPolicy retryPolicy = RetryPolicy.builder().build();

    assertThat(retryPolicy.getMaxAttempts()).isEqualTo(5);
    assertThat(retryPolicy.getInitialBackoff()).isEqualTo(Duration.ofSeconds(1));
    assertThat(retryPolicy.getMaxBackoff()).isEqualTo(Duration.ofSeconds(5));
    assertThat(retryPolicy.getBackoffMultiplier()).isEqualTo(1.5);

    assertThat(RetryPolicy.getDefault()).isEqualTo(retryPolicy);
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
    assertThat(retryPolicy.getRetryExceptionPredicate()).isEqualTo(null);
  }

  @Test
  void invalidRetryPolicy() {
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxAttempts(1).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxAttempts(6).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setInitialBackoff(null).build())
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setInitialBackoff(Duration.ofMillis(0)).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxBackoff(null).build())
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setMaxBackoff(Duration.ofMillis(0)).build())
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> RetryPolicy.builder().setBackoffMultiplier(0).build())
        .isInstanceOf(IllegalArgumentException.class);
  }
}
