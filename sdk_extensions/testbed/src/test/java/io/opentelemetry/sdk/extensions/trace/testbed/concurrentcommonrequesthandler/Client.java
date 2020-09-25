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

package io.opentelemetry.sdk.extensions.trace.testbed.concurrentcommonrequesthandler;

import io.opentelemetry.sdk.extensions.trace.testbed.TestUtils;
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
    final RequestHandlerContext requestHandlerContext = new RequestHandlerContext();
    return executor.submit(
        () -> {
          TestUtils.sleep();
          executor
              .submit(
                  () -> {
                    TestUtils.sleep();
                    requestHandler.beforeRequest(message, requestHandlerContext);
                  })
              .get();

          executor
              .submit(
                  () -> {
                    TestUtils.sleep();
                    requestHandler.afterResponse(message, requestHandlerContext);
                  })
              .get();

          return message + ":response";
        });
  }
}
