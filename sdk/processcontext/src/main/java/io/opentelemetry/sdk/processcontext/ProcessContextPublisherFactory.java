/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import java.util.logging.Logger;

public class ProcessContextPublisherFactory {

  private static final Logger logger =
      Logger.getLogger(ProcessContextPublisherFactory.class.getName());

  private static final ProcessContextPublisherFactory INSTANCE =
      new ProcessContextPublisherFactory();

  // effectively a singleton, via the factory INSTANCE - java won't do direct interface type
  // singletons :-(
  private final ProcessContextPublisher processContextPublisher;

  private ProcessContextPublisherFactory() {

    ProcessContextPublisher tmp = new NoopProcessContextPublisher();

    // The publisher uses linux specific syscalls via panama FFM, requiring a sufficiently recent
    // JDK.
    boolean available =
        System.getProperty("os.name").equalsIgnoreCase("Linux")
            && Integer.parseInt(System.getProperty("java.specification.version")) >= 25;

    // off by default until it's considered stable.
    boolean enabled =
        System.getProperty("otel.processcontext.publish", "false").equalsIgnoreCase("true");

    if (available && enabled) {
      try {
        tmp =
            (ProcessContextPublisher)
                Class.forName("io.opentelemetry.sdk.processcontext.PanamaProcessContextPublisher")
                    .getConstructors()[0]
                    .newInstance((Object[]) null);
      } catch (Exception e) {
        logger.warning("Failed to init ProcessContextPublisher: " + e.getMessage());
      }
    }

    processContextPublisher = tmp;
  }

  public static ProcessContextPublisherFactory getInstance() {
    return INSTANCE;
  }

  public ProcessContextPublisher create() {
    return processContextPublisher;
  }
}
