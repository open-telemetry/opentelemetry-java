/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestSourceInfo {
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

  @Test
  void sourceInfoUsesCustomValues() {
    SourceInfo info = SourceInfo.fromConfigFile("mypath.yml", 20);
    assertThat(info.shortDebugString()).isEqualTo("mypath.yml:20");
    assertThat(info.multiLineDebugString()).isEqualTo("\tat mypath.yml:20");
  }
}
