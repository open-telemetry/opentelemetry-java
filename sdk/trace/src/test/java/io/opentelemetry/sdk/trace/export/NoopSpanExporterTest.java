/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NoopSpanExporterTest {

  @Test
  void stringRepresentation() {
    assertThat(NoopSpanExporter.getInstance()).hasToString("NoopSpanExporter{}");
  }
}
