/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class GrpcJavaOtlpIntegrationTest extends OtlpExporterIntegrationTest {

  @Test
  void noGrpcFound() {
    assertThatCode(() -> Class.forName("io.grpc.ManagedChannel")).doesNotThrowAnyException();
  }
}
