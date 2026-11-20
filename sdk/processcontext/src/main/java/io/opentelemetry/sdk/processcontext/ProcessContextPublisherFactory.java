/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for obtaining a {@link ProcessContextPublisher}.
 *
 * <p>The supplied publisher will, depending on the Java version and OpenTelemetry configuration, be
 * either a no-op or an implementation using calls to linux libc.
 */
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
    // Config work in progress: OTEL_EXPERIMENTAL_PROCESS_CONTEXT_ENABLED per
    // https://github.com/open-telemetry/opentelemetry-specification/pull/5341
    // and https://github.com/open-telemetry/opentelemetry-specification/pull/5370
    boolean enabled =
        System.getProperty("otel.experimental.process.context.enabled", "false")
            .equalsIgnoreCase("true");

    if (available && enabled) {
      try {
        tmp =
            (ProcessContextPublisher)
                Class.forName("io.opentelemetry.sdk.processcontext.PanamaProcessContextPublisher")
                    .getConstructors()[0]
                    .newInstance((Object[]) null);
      } catch (Exception e) {
        logger.log(Level.WARNING, "Failed to init ProcessContextPublisher", e.getMessage());
      }
    }

    if (enabled && !available) {
      logger.log(
          Level.WARNING,
          "Process Context publication requested, but no suitable implementation available for this environment");
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
