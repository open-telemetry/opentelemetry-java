/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class ViewUtil {

  private ViewUtil() {}

  /**
   * Add an {@link AttributesProcessor} to the {@link ViewBuilder} which appends key-values from
   * baggage to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param viewBuilder the builder
   * @param keyFilter Only baggage key values pairs where the key matches this predicate will be
   *     appended.
   */
  public static void appendFilteredBaggageAttributes(
      ViewBuilder viewBuilder, Predicate<String> keyFilter) {
    addAttributesProcessor(viewBuilder, AttributesProcessor.appendBaggageByKeyName(keyFilter));
  }

  /**
   * Add an {@link AttributesProcessor} to the {@link ViewBuilder} which which appends all
   * key-values from baggage to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param viewBuilder the builder
   */
  public static void appendAllBaggageAttributes(ViewBuilder viewBuilder) {
    appendFilteredBaggageAttributes(viewBuilder, StringPredicates.ALL);
  }

  private static void addAttributesProcessor(
      ViewBuilder viewBuilder, AttributesProcessor attributesProcessor) {
    try {
      Method method =
          ViewBuilder.class.getDeclaredMethod("addAttributesProcessor", AttributesProcessor.class);
      method.setAccessible(true);
      method.invoke(viewBuilder, attributesProcessor);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException("Error adding AttributesProcessor to ViewBuilder", e);
    }
  }
}
