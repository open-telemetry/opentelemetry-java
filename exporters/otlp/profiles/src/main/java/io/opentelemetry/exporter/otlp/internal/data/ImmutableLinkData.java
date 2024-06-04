/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.LinkData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableLinkData implements LinkData {

  public static LinkData create(String traceId, String spanId) {
    return new AutoValue_ImmutableLinkData(traceId, spanId);
  }

  ImmutableLinkData() {}
}
