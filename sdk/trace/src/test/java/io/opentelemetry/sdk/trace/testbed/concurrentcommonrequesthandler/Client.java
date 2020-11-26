/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.concurrentcommonrequesthandler;

import io.opentelemetry.sdk.trace.testbed.TestUtils;
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
