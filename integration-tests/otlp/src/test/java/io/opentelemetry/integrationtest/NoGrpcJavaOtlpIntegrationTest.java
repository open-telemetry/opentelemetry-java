/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NoGrpcJavaOtlpIntegrationTest extends OtlpExporterIntegrationTest {

  @Test
  void noGrpcFound() {
    assertThatThrownBy(() -> Class.forName("io.grpc.ManagedChannel"))
        .isInstanceOf(ClassNotFoundException.class);
  }
}
