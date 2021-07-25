/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Collection of factory methods to construct {@link AttributesProcessor}s. */
final class AttributesProcessors {
  private AttributesProcessors() {}

  /**
   * Creates an {@link AttributesProcessor} that filters incoming keys.
   *
   * @param keyPattern Regex where all matching incoming keys are preserved.
   */
  public static AttributesProcessor filterKeysByPattern(Pattern keyPattern) {
    return new SimpleAttributesProcessor() {
      @Override
      @SuppressWarnings("unchecked")
      // We are unable to get existentials working for attribute k-v pairs without
      // better type gymnastics in the core library.
      protected Attributes proocess(Attributes incoming) {
        AttributesBuilder result = Attributes.builder();
        incoming.forEach(
            (k, v) -> {
              if (keyPattern.matcher(k.getKey()).matches()) {
                result.put((AttributeKey<Object>) k, v);
              }
            });
        return result.build();
      }

      @Override
      public final String toString() {
        return "filterKeysByPattern(" + keyPattern + ")";
      }
    };
  }

    /**
   * Creates an {@link AttributesProcessor} that filters incoming keys.
   *
   * @param keys List of all keys to preserve.
   */
  public static AttributesProcessor filterKeys(AttributeKey<?> ...keys) {
    final Set<AttributeKey<?>> keyCheck = new HashSet<>();
    for (AttributeKey<?> key : keys) {
      keyCheck.add(key);
    }
    return new SimpleAttributesProcessor() {
      @Override
      @SuppressWarnings("unchecked")
      // We are unable to get existentials working for attribute k-v pairs without
      // better type gymnastics in the core library.
      protected Attributes proocess(Attributes incoming) {
        AttributesBuilder result = Attributes.builder();
        incoming.forEach(
            (k, v) -> {
              if (keyCheck.contains(k)) {
                result.put((AttributeKey<Object>) k, v);
              }
            });
        return result.build();
      }

      @Override
      public final String toString() {
        return "filterKeysByPattern(" + keyCheck + ")";
      }
    };
  }
  

  /**
   * Creates an {@link AttributesProcessor} the appends attributes to existing metrics.
   *
   * @param append A set of attributes to append to metrics if they don't already exist.
   */
  public static AttributesProcessor appendAttributes(final Attributes append) {
    return new SimpleAttributesProcessor() {
      @Override
      protected Attributes proocess(Attributes incoming) {
        // We start with append, so all incoming attributes override the append list
        // on `.putAll` method.
        return append.toBuilder().putAll(incoming).build();
      }
    };
  }

 /**
   * Creates an {@link AttributesProcessor} the appends any matching baggage keys.
   *
   * @param keys list of exact match keys to append.
   */
  public static AttributesProcessor appendBaggageByKeys(final String ...keys) {
    final Set<String> keyCheck = new HashSet<>();
    for (String key : keys) {
      keyCheck.add(key);
    }
    return new BaggageAttributesProcessor() {
      
      @Override
      protected Attributes process(Attributes attributes, Baggage baggage) {
        AttributesBuilder result = Attributes.builder();
        baggage.forEach(
            (k, v) -> {
              if (keyCheck.contains(k)) {
                result.put(k, v.getValue());
              }
            });
        // Override any baggage keys with existing keys.
        result.putAll(attributes);
        return result.build();
      }

      @Override
      public final String toString() {
        return "appendBaggageByKeys(" + keyCheck + ")";
      }
    };
  }

  /**
   * Creates an {@link AttributesProcessor} the appends any matching baggage keys.
   *
   * @param keyPattern Regex where all matching baggage keys are included.
   */
  public static AttributesProcessor appendBaggageByKeyPattern(final Pattern keyPattern) {
    return new BaggageAttributesProcessor() {
      @Override
      protected Attributes process(Attributes attributes, Baggage baggage) {
        AttributesBuilder result = Attributes.builder();
        baggage.forEach(
            (k, v) -> {
              if (keyPattern.matcher(k).matches()) {
                result.put(k, v.getValue());
              }
            });
        // Override any baggage keys with existing keys.
        result.putAll(attributes);
        return result.build();
      }

      @Override
      public final String toString() {
        return "appendBaggageByKeyPattern(" + keyPattern + ")";
      }
    };
  }
  /**
   * Creates an {@link AttributesProcessor} that appends all baggage values to metric attributes.
   */
  public static AttributesProcessor appendBaggage() {
    return appendBaggageByKeyPattern(Pattern.compile(".*"));
  }
}
