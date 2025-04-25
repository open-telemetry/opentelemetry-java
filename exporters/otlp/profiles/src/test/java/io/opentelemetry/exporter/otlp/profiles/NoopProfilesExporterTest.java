/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NoopProfilesExporterTest {

  @Test
  void stringRepresentation() {
    assertThat(NoopProfilesExporter.getInstance()).hasToString("NoopProfilesExporter{}");
  }
}
