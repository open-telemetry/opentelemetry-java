/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class StressTestRunner {
  abstract ImmutableList<Operation> getOperations();

  abstract TracerSdk getTracer();

  abstract SpanProcessor getSpanProcessor();

  final void run() {
    List<Operation> operations = getOperations();
    int numThreads = operations.size();
    final CountDownLatch countDownLatch = new CountDownLatch(numThreads);
    List<Thread> operationThreads = new ArrayList<>(numThreads);
    for (final Operation operation : operations) {
      operationThreads.add(
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  for (int i = 0; i < operation.getNumOperations(); i++) {
                    operation.getUpdater().update();
                    Uninterruptibles.sleepUninterruptibly(
                        operation.getOperationDelayMs(), TimeUnit.MILLISECONDS);
                  }
                  countDownLatch.countDown();
                }
              }));
    }

    for (Thread thread : operationThreads) {
      thread.start();
    }

    // Wait for all the threads to finish.
    for (Thread thread : operationThreads) {
      Uninterruptibles.joinUninterruptibly(thread);
    }
    getSpanProcessor().shutdown();
  }

  static Builder builder() {
    return new AutoValue_StressTestRunner.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setTracer(TracerSdk tracerSdk);

    abstract ImmutableList.Builder<Operation> operationsBuilder();

    abstract Builder setSpanProcessor(SpanProcessor spanProcessor);

    Builder addOperation(final Operation operation) {
      operationsBuilder().add(operation);
      return this;
    }

    public abstract StressTestRunner build();
  }

  @AutoValue
  @Immutable
  abstract static class Operation {

    abstract int getNumOperations();

    abstract int getOperationDelayMs();

    abstract OperationUpdater getUpdater();

    static Operation create(int numOperations, int operationDelayMs, OperationUpdater updater) {
      return new AutoValue_StressTestRunner_Operation(numOperations, operationDelayMs, updater);
    }
  }

  interface OperationUpdater {
    /** Called every operation. */
    void update();
  }

  StressTestRunner() {}
}
