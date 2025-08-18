/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class DaemonThreadFactoryTest {

  @Test
  void verifyUncaughtExceptions()
      throws ExecutionException, InterruptedException, TimeoutException {
    Thread.UncaughtExceptionHandler defaultHandler = mock();
    ThreadFactory delegateFactory =
        r -> {
          Thread thread = Executors.defaultThreadFactory().newThread(r);
          thread.setUncaughtExceptionHandler(defaultHandler);
          return thread;
        };
    ExecutorService service =
        Executors.newSingleThreadExecutor(new DaemonThreadFactory("test", delegateFactory));
    Callable<Boolean> callable =
        () -> {
          Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
              Thread.currentThread().getUncaughtExceptionHandler();

          assertThat(uncaughtExceptionHandler)
              .isInstanceOf(DaemonThreadFactory.ManagedUncaughtExceptionHandler.class);

          Thread threadMock = mock();

          // Verify interrupted exception
          uncaughtExceptionHandler.uncaughtException(threadMock, new InterruptedException());
          verify(threadMock).interrupt();
          verifyNoInteractions(defaultHandler);

          // Verify delegate exception
          clearInvocations(threadMock, defaultHandler);
          IllegalStateException e = new IllegalStateException();
          uncaughtExceptionHandler.uncaughtException(threadMock, e);
          verify(defaultHandler).uncaughtException(threadMock, e);
          return true;
        };

    assertThat(service.submit(callable).get(5, TimeUnit.SECONDS)).isTrue();
  }
}
