/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TraceConfigTest {

  @Test
  void defaultTraceConfig() {
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributes()).isEqualTo(1000);
    assertThat(TraceConfig.getDefault().getMaxNumberOfEvents()).isEqualTo(1000);
    assertThat(TraceConfig.getDefault().getMaxNumberOfLinks()).isEqualTo(1000);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink()).isEqualTo(32);
  }

  @Test
  void updateTraceConfig_All() {
    TraceConfig traceConfig =
        TraceConfig.builder()
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfEvents(10)
            .setMaxNumberOfLinks(11)
            .setMaxNumberOfAttributesPerEvent(1)
            .setMaxNumberOfAttributesPerLink(2)
            .build();
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(10);
    assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(11);
    assertThat(traceConfig.getMaxNumberOfAttributesPerEvent()).isEqualTo(1);
    assertThat(traceConfig.getMaxNumberOfAttributesPerLink()).isEqualTo(2);

    // Preserves values
    TraceConfig traceConfigDupe = traceConfig.toBuilder().build();
    // Use reflective comparison to catch when new fields are added.
    assertThat(traceConfigDupe).usingRecursiveComparison().isEqualTo(traceConfig);
  }
}
