/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.util.concurrent.ThreadPoolExecutor;
import okhttp3.Dispatcher;
import org.junit.jupiter.api.Test;

class OkHttpUtilTest {

  @Test
  void newDispatcher_isBounded() {
    Dispatcher dispatcher = OkHttpUtil.newDispatcher();

    try {
      assertThat(dispatcher.getMaxRequests()).isEqualTo(64);
      assertThat(dispatcher.getMaxRequestsPerHost()).isEqualTo(5);
      assertThat(dispatcher.executorService())
          .asInstanceOf(type(ThreadPoolExecutor.class))
          .satisfies(
              executor -> {
                assertThat(executor.getMaximumPoolSize()).isEqualTo(64);
                assertThat(executor.getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);
              });
    } finally {
      dispatcher.executorService().shutdownNow();
    }
  }
}
