/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.statelesscommonrequesthandler;

import io.opentelemetry.opentracingshim.testbed.TestUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Client {
  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private final ExecutorService executor = Executors.newCachedThreadPool();

  private final RequestHandler requestHandler;

  public Client(RequestHandler requestHandler) {
    this.requestHandler = requestHandler;
  }

  /** Send a request....... */
  public Future<String> send(Object message) {

    return executor.submit(
        () -> {
          logger.info("send {}", message);
          TestUtils.sleep();
          requestHandler.beforeRequest(message);

          TestUtils.sleep();
          requestHandler.afterResponse(message);

          return message + ":response";
        });
  }
}
