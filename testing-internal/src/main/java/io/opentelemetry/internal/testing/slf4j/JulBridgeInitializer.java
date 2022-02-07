/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.internal.testing.slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.LogManager;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** Sets {@link java.util.logging.Logger} to use the SLF4J bridge. */
public final class JulBridgeInitializer {
  /** Invoked by JUL automatically to read configuration. */
  public JulBridgeInitializer() throws IOException {
    String config = "handlers = " + SLF4JBridgeHandler.class.getName();
    LogManager.getLogManager()
        .readConfiguration(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
  }
}
