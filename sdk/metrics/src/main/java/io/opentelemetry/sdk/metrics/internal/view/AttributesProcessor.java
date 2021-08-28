/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import java.util.Arrays;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

/**
 * An {@code AttributesProcessor} is used by {@code View}s to define the actual recorded set of
 * attributes.
 *
 * <p>An AttributesProcessor is used to define the actual set of attributes that will be used in a
 * Metric vs. the inbound set of attributes from a measurement.
 */
@FunctionalInterface
@Immutable
public interface AttributesProcessor {

  /**
   * Manipulates a set of attributes, returning the desired set.
   *
   * @param incoming Attributes assocaited with an incoming measurement.
   * @param context The context associated with the measurement.
   */
  Attributes process(Attributes incoming, Context context);

  /**
   * If true, this ensures the `Context` argument of the attributes processor is always acurate.
   * This will prevents bound instruments from pre-locking their metric-attributes and defer until
   * context is available.
   */
  default boolean usesContext() {
    return true;
  }

  /** Joins this attribute processor with another that operates after this one. */
  default AttributesProcessor then(AttributesProcessor other) {
    if (other == NOOP) {
      return this;
    }
    if (this == NOOP) {
      return other;
    }
    if (other instanceof JoinedAttribtuesProcessor) {
      return ((JoinedAttribtuesProcessor) other).prepend(this);
    }
    return new JoinedAttribtuesProcessor(Arrays.asList(this, other));
  }

  /** No-op version of attributes processer, returns what it gets. */
  public static AttributesProcessor noop() {
    return NOOP;
  }

  /**
   * Creates a processor which filters down attributes from a measurement.
   *
   * @param nameFilter a filter for which attribute keys to preserve.
   */
  @SuppressWarnings("unchecked")
  public static AttributesProcessor filterByKeyName(Predicate<String> nameFilter) {
    return new SimpleAttributesProcessor() {
      @Override
      protected Attributes process(Attributes incoming) {
        AttributesBuilder result = Attributes.builder();
        incoming.forEach(
            (k, v) -> {
              if (nameFilter.test(k.getKey())) {
                result.put((AttributeKey<Object>) k, v);
              }
            });
        return result.build();
      }

      @Override
      public String toString() {
        return "KeyNameFilterProcessor";
      }
    };
  }

  /**
   * Creates a processor which appends values from {@link Baggage}.
   *
   * <p>These attributes will not override those attributes provided by instrumentation.
   *
   * @param nameFilter a filter for which baggage keys to select.
   */
  public static AttributesProcessor appendBaggageByKeyName(Predicate<String> nameFilter) {
    return new BaggageAttributesProcessor() {

      @Override
      protected Attributes process(Attributes incoming, Baggage baggage) {
        AttributesBuilder result = Attributes.builder();
        baggage.forEach(
            (k, v) -> {
              if (nameFilter.test(k)) {
                result.put(k, v.getValue());
              }
            });
        // Override any baggage keys with existing keys.
        result.putAll(incoming);
        return result.build();
      }

      @Override
      public String toString() {
        return "BaggageAppendProcessor";
      }
    };
  }

  /**
   * Creates a processor which appends (exactly) the given attributes.
   *
   * <p>These attributes will not override those attributes provided by instrumentation.
   *
   * @param attributes Attributes to append to measurements.
   */
  public static AttributesProcessor append(Attributes attributes) {
    return new SimpleAttributesProcessor() {

      @Override
      protected Attributes process(Attributes incoming) {
        return attributes.toBuilder().putAll(incoming).build();
      }

      @Override
      public String toString() {
        return "AppendAttributesProcessor";
      }
    };
  }

  static final AttributesProcessor NOOP =
      new AttributesProcessor() {
        @Override
        public Attributes process(Attributes incoming, Context context) {
          return incoming;
        }

        @Override
        public boolean usesContext() {
          return false;
        }

        @Override
        public String toString() {
          return "NoopAttributesProcessor";
        }
      };
}
