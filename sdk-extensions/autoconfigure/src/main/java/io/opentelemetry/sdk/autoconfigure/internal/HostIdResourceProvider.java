/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/** {@link ResourceProvider} for automatically configuring <code>host.id</code>. */
public final class HostIdResourceProvider implements ConditionalResourceProvider {

  private static final Logger logger = Logger.getLogger(HostIdResourceProvider.class.getName());

  public static final AttributeKey<String> HOST_ID = AttributeKey.stringKey("host.id");

  private final Function<Path, List<String>> pathReader;

  public HostIdResourceProvider() {
    this(
        path -> {
          try {
            return Files.readAllLines(path);
          } catch (IOException e) {
            throw new IllegalStateException(e);
          }
        });
  }

  // Visible for testing
  HostIdResourceProvider(Function<Path, List<String>> pathReader) {
    this.pathReader = pathReader;
  }

  @Override
  public Resource createResource(ConfigProperties config) {
    Path path = FileSystems.getDefault().getPath("/etc/machine-id");
    try {
      List<String> lines = pathReader.apply(path);
      if (lines.isEmpty()) {
        logger.fine("Failed to read /etc/machine-id: empty file");
        return Resource.empty();
      }
      return Resource.create(Attributes.of(HOST_ID, lines.get(0)));
    } catch (RuntimeException e) {
      logger.log(Level.FINE, "Failed to read /etc/machine-id", e);
      return Resource.empty();
    }
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    return !config.getMap("otel.resource.attributes").containsKey(HOST_ID.getKey());
  }

  @Override
  public int order() {
    // Run after cloud provider resource providers
    return Integer.MAX_VALUE - 1;
  }
}
