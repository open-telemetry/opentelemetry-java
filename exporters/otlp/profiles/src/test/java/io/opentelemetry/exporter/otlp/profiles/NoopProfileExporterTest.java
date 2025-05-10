/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NoopProfileExporterTest {

  @Test
  void stringRepresentation() {
    assertThat(NoopProfileExporter.getInstance()).hasToString("NoopProfilesExporter{}");
  }
}
