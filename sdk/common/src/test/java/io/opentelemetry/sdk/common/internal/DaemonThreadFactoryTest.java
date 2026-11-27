/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // Testing deprecated code
class DaemonThreadFactoryTest {

  @Test
  void newThread() {
    ThreadFactory factory1 = new DaemonThreadFactory("test-internal-1");
    Thread thread1 = factory1.newThread(() -> {});
    assertThat(thread1.isDaemon()).isTrue();
    assertThat(thread1.getName()).startsWith("test-internal-1-");

    ThreadFactory factory2 =
        new DaemonThreadFactory("test-internal-2", Executors.defaultThreadFactory());
    Thread thread2 = factory2.newThread(() -> {});
    assertThat(thread2.isDaemon()).isTrue();
    assertThat(thread2.getName()).startsWith("test-internal-2-");
  }
}
