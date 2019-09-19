/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.contrib.trace.testbed.concurrentcommonrequesthandler;

import io.opentelemetry.sdk.contrib.trace.testbed.TestUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class Client {
  private final ExecutorService executor = Executors.newCachedThreadPool();

  private final RequestHandler requestHandler;

  public Client(RequestHandler requestHandler) {
    this.requestHandler = requestHandler;
  }

  public Future<String> send(final Object message) {
    final Context context = new Context();
    return executor.submit(
        new Callable<String>() {
          @Override
          public String call() throws Exception {
            TestUtils.sleep();
            executor
                .submit(
                    new Runnable() {
                      @Override
                      public void run() {
                        TestUtils.sleep();
                        requestHandler.beforeRequest(message, context);
                      }
                    })
                .get();

            executor
                .submit(
                    new Runnable() {
                      @Override
                      public void run() {
                        TestUtils.sleep();
                        requestHandler.afterResponse(message, context);
                      }
                    })
                .get();

            return message + ":response";
          }
        });
  }
}
