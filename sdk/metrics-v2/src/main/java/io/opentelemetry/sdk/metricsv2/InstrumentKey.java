/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

@AutoValue
abstract class InstrumentKey {
  static InstrumentKey create(
      InstrumentDescriptor descriptor, InstrumentationLibraryInfo libraryInfo) {
    return new AutoValue_InstrumentKey(descriptor, libraryInfo);
  }

  abstract InstrumentDescriptor instrumentDescriptor();

  abstract InstrumentationLibraryInfo libraryInfo();
}
