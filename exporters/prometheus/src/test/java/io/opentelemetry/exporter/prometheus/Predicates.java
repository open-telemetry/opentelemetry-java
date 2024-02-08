/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import java.util.function.Predicate;

public final class Predicates {

  static final Predicate<String> ALLOW_ALL = attributeKey -> true;

  private Predicates() {}

  @SuppressWarnings("SameParameterValue")
  static Predicate<String> startsWith(String prefix) {
    return attributeKey -> attributeKey.startsWith(prefix);
  }

  public static Predicate<String> is(String value) {
    return attributeKey -> attributeKey.equals(value);
  }
}
