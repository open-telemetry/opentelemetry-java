/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class BatchSettingsTest {

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void buildSettings() {
    BatchSettings settings =
        BatchSettings.builder()
            .setScheduleDelay(3, TimeUnit.SECONDS)
            .setExporterTimeout(4, TimeUnit.SECONDS)
            .setMaxQueueSize(10)
            .setMaxExportBatchSize(2)
            .build();
    assertThat(settings.getScheduleDelayNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(3));
    assertThat(settings.getExporterTimeoutNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(4));
    assertThat(settings.getMaxQueueSize()).isEqualTo(10);
    assertThat(settings.getMaxExportBatchSize()).isEqualTo(2);
  }

  @Test
  void buildSettings_javaTime() {
    BatchSettings settings =
        BatchSettings.builder()
            .setScheduleDelay(Duration.ofSeconds(3))
            .setExporterTimeout(Duration.ofSeconds(4))
            .setMaxQueueSize(10)
            .setMaxExportBatchSize(2)
            .build();
    assertThat(settings.getScheduleDelayNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(3));
    assertThat(settings.getExporterTimeoutNanos()).isEqualTo(TimeUnit.SECONDS.toNanos(4));
    assertThat(settings.getMaxQueueSize()).isEqualTo(10);
    assertThat(settings.getMaxExportBatchSize()).isEqualTo(2);
  }
}
