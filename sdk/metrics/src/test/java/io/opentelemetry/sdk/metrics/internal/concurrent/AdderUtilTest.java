/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdderUtilTest {

  @Test
  void jreAdder() {
    assertThat(AdderUtil.createDoubleAdder()).isInstanceOf(JreDoubleAdder.class);
    assertThat(AdderUtil.createLongAdder()).isInstanceOf(JreLongAdder.class);
  }
}
