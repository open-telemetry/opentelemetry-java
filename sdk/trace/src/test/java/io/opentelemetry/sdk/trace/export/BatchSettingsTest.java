/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class BatchSettingsTest {

  @Test
  void buildSettings() {
    BatchSettings settings =
        BatchSettings.builder()
            .setScheduleDelay(3, TimeUnit.SECONDS)
            .setExporterTimeout(4, TimeUnit.SECONDS)
            .setMaxQueueSize(10)
            .setMaxExportBatchSize(2)
            .build();
    assertThat(settings.getScheduleDelayMillis()).isEqualTo(3000);
    assertThat(settings.getExporterTimeoutMillis()).isEqualTo(4000);
    assertThat(settings.getMaxQueueSize()).isEqualTo(10);
    assertThat(settings.getMaxExportBatchSize()).isEqualTo(2);
  }
}
