/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.statelesscommonrequesthandler;

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

  /** Send a request....... */
  public Future<String> send(final Object message) {

    return executor.submit(
        () -> {
          TestUtils.sleep();
          requestHandler.beforeRequest(message);

          TestUtils.sleep();
          requestHandler.afterResponse(message);

          return message + ":response";
        });
  }
}
