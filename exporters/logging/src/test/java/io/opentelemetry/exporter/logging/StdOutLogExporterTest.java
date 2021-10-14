/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class StdOutLogExporterTest {

  @Test
  void returnCode() {
    StdOutLogExporter exporter = new StdOutLogExporter();
    CompletableResultCode resultCode = exporter.export(Collections.emptyList());
    assertThat(resultCode).isSameAs(CompletableResultCode.ofSuccess());
  }
}
