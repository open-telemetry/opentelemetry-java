/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Collection;

final class NoopProfilesExporter implements ProfilesExporter {

  private static final ProfilesExporter INSTANCE = new NoopProfilesExporter();

  static ProfilesExporter getInstance() {
    return INSTANCE;
  }

  @Override
  public CompletableResultCode export(Collection<ProfileData> spans) {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    return "NoopProfilesExporter{}";
  }
}
