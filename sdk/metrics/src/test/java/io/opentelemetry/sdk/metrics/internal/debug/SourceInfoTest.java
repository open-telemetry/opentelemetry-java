/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SourceInfoTest {
  @Test
  void noSourceInfo_includesEnableDebugMessage() {
    assertThat(SourceInfo.noSourceInfo().multiLineDebugString())
        .contains(DebugConfig.getHowToEnableMessage());
  }

  @Test
  void doesNotGrabStackWhenDisabled() {
    DebugConfig.enableForTesting(false);
    assertThat(SourceInfo.fromCurrentStack()).isInstanceOf(NoSourceInfo.class);
  }

  @Test
  void doesGrabStackWhenEnabled() {
    DebugConfig.enableForTesting(true);
    assertThat(SourceInfo.fromCurrentStack()).isInstanceOf(StackTraceSourceInfo.class);
  }
}
