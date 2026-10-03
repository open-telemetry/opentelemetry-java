/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Factories for {@link UnaryOperator} instances that filter {@link Attributes}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AttributesFilters {

  /** A filter that returns the input attributes unchanged. */
  public static final UnaryOperator<Attributes> ALLOW_ALL = attrs -> attrs;

  private AttributesFilters() {}

  /**
   * Returns a filter that retains only attributes whose key name passes {@code keyFilter}. Returns
   * {@link #ALLOW_ALL} when {@code keyFilter} is {@link StringPredicates#ALL}.
   */
  @SuppressWarnings("ReferenceEquality")
  public static UnaryOperator<Attributes> byKeyName(Predicate<String> keyFilter) {
    if (keyFilter == StringPredicates.ALL) {
      return ALLOW_ALL;
    }
    return attrs -> attrs.toBuilder().removeIf(k -> !keyFilter.test(k.getKey())).build();
  }

  /**
   * Returns a filter that retains only attributes whose key matches one of {@code adviceKeys} (by
   * both name and type). Uses {@link FilteredAttributes} to avoid rebuilding the attributes map.
   */
  public static UnaryOperator<Attributes> byAdvice(List<AttributeKey<?>> adviceKeys) {
    Set<AttributeKey<?>> keys = new HashSet<>(adviceKeys);
    return attrs -> FilteredAttributes.create(attrs, keys);
  }
}
