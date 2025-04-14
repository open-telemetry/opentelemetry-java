/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ProfilesExporter} that forwards all received data to a list of {@link ProfilesExporter}s.
 */
final class MultiProfilesExporter implements ProfilesExporter {
  private static final Logger logger = Logger.getLogger(MultiProfilesExporter.class.getName());

  private final ProfilesExporter[] profilesExporters;

  /**
   * Constructs and returns an instance of this class.
   *
   * @param profilesExporters the exporters data should be sent to
   * @return the aggregate data exporter
   */
  static ProfilesExporter create(List<ProfilesExporter> profilesExporters) {
    return new MultiProfilesExporter(profilesExporters.toArray(new ProfilesExporter[0]));
  }

  @Override
  public CompletableResultCode export(Collection<ProfileData> items) {
    List<CompletableResultCode> results = new ArrayList<>(profilesExporters.length);
    for (ProfilesExporter profilesExporter : profilesExporters) {
      CompletableResultCode exportResult;
      try {
        exportResult = profilesExporter.export(items);
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the export.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(exportResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  /**
   * Flushes the data of all registered {@link ProfilesExporter}s.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    List<CompletableResultCode> results = new ArrayList<>(profilesExporters.length);
    for (ProfilesExporter profilesExporter : profilesExporters) {
      CompletableResultCode flushResult;
      try {
        flushResult = profilesExporter.flush();
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the flush.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(flushResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode shutdown() {
    List<CompletableResultCode> results = new ArrayList<>(profilesExporters.length);
    for (ProfilesExporter profilesExporter : profilesExporters) {
      CompletableResultCode shutdownResult;
      try {
        shutdownResult = profilesExporter.shutdown();
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the shutdown.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(shutdownResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  private MultiProfilesExporter(ProfilesExporter[] profilesExporters) {
    this.profilesExporters = profilesExporters;
  }

  @Override
  public String toString() {
    return "MultiProfilesExporter{"
        + "profilesExporters="
        + Arrays.toString(profilesExporters)
        + '}';
  }
}
