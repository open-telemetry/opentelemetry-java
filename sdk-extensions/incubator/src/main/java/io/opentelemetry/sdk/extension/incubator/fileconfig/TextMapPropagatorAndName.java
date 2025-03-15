/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.context.propagation.TextMapPropagator;

@AutoValue
abstract class TextMapPropagatorAndName {

  static TextMapPropagatorAndName create(TextMapPropagator textMapPropagator, String name) {
    return new AutoValue_TextMapPropagatorAndName(textMapPropagator, name);
  }

  abstract TextMapPropagator getTextMapPropagator();

  abstract String getName();
}
