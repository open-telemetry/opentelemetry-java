/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

/**
 * Utilities for configuring the output stream of the OTLP file exporters.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OutputStreamConfigUtil {

  private static final String STDOUT = "stdout";
  private static final String FILE_SCHEME = "file";

  /**
   * Invoke the {@code outputStreamConsumer} with the configured output stream.
   *
   * <p>Recognized values are {@code stdout} and a file URI such as {@code
   * file:///path/to/file.jsonl}. Missing parent directories of the file are created, and the file
   * is appended to if it already exists.
   */
  @SuppressWarnings("SystemOut")
  public static void configureOutputStream(
      DeclarativeConfigProperties config, Consumer<OutputStream> outputStreamConsumer) {
    String outputStream = config.getString("output_stream");
    if (outputStream == null) {
      return;
    }
    if (STDOUT.equalsIgnoreCase(outputStream)) {
      outputStreamConsumer.accept(System.out);
      return;
    }
    Path path = filePath(outputStream);
    try {
      Path parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      outputStreamConsumer.accept(
          new BufferedOutputStream(
              Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)));
    } catch (IOException e) {
      throw new ConfigurationException("Unable to open output_stream: " + outputStream, e);
    }
  }

  private static Path filePath(String outputStream) {
    URI uri;
    try {
      uri = new URI(outputStream);
    } catch (URISyntaxException e) {
      throw new ConfigurationException("Unrecognized output_stream: " + outputStream, e);
    }
    if (!FILE_SCHEME.equalsIgnoreCase(uri.getScheme())) {
      throw new ConfigurationException("Unrecognized output_stream: " + outputStream);
    }
    try {
      return Paths.get(uri);
    } catch (IllegalArgumentException | FileSystemNotFoundException e) {
      throw new ConfigurationException("Unrecognized output_stream: " + outputStream, e);
    }
  }

  private OutputStreamConfigUtil() {}
}
