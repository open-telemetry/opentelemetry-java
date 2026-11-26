/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import org.junit.jupiter.api.Test;

class RetryUtilTest {

  @Test
  void retryableGrpcStatusCodes() {
    assertThat(RetryUtil.retryableGrpcStatusCodes())
        .containsExactlyInAnyOrder("1", "4", "8", "10", "11", "14", "15");
  }

  @Test
  void retryableGrpcStatusCodeEnums() {
    assertThat(RetryUtil.retryableGrpcStatusCodeEnums())
        .containsExactlyInAnyOrder(
            GrpcStatusCode.CANCELLED,
            GrpcStatusCode.DEADLINE_EXCEEDED,
            GrpcStatusCode.RESOURCE_EXHAUSTED,
            GrpcStatusCode.ABORTED,
            GrpcStatusCode.OUT_OF_RANGE,
            GrpcStatusCode.UNAVAILABLE,
            GrpcStatusCode.DATA_LOSS);
  }

  @Test
  void retryableHttpResponseCodes() {
    assertThat(RetryUtil.retryableHttpResponseCodes())
        .containsExactlyInAnyOrder(429, 502, 503, 504);
  }
}
