/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.sdk.metrics.internal.view.NoopAttributesProcessor.NOOP;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.concurrent.Immutable;

/**
 * An AttributesProcessor is used to define the actual set of attributes that will be used in a
 * Metric vs. the inbound set of attributes from a measurement.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public abstract class AttributesProcessor {

  AttributesProcessor() {}

  /**
   * Manipulates a set of attributes, returning the desired set.
   *
   * @param incoming Attributes associated with an incoming measurement.
   * @param context The context associated with the measurement.
   */
  public abstract Attributes process(Attributes incoming, Context context);

  /**
   * If true, this ensures the `Context` argument of the attributes processor is always accurate.
   * This will prevents bound instruments from pre-locking their metric-attributes and defer until
   * context is available.
   */
  public abstract boolean usesContext();

  /** Joins this attribute processor with another that operates after this one. */
  public AttributesProcessor then(AttributesProcessor other) {
    if (other == NOOP) {
      return this;
    }
    if (this == NOOP) {
      return other;
    }

    if (other instanceof JoinedAttributesProcessor) {
      return ((JoinedAttributesProcessor) other).prepend(this);
    }
    return new JoinedAttributesProcessor(Arrays.asList(this, other));
  }

  /** No-op version of attributes processor, returns what it gets. */
  public static AttributesProcessor noop() {
    return NOOP;
  }

  /**
   * Creates a processor which filters down attributes from a measurement.
   *
   * @param nameFilter a filter for which attribute keys to preserve.
   */
  public static AttributesProcessor filterByKeyName(Predicate<String> nameFilter) {
    return simple(
        incoming ->
            incoming.toBuilder()
                .removeIf(attributeKey -> !nameFilter.test(attributeKey.getKey()))
                .build());
  }

  /**
   * Creates a processor which appends values from {@link Baggage}.
   *
   * <p>These attributes will not override those attributes provided by instrumentation.
   *
   * @param nameFilter a filter for which baggage keys to select.
   */
  public static AttributesProcessor appendBaggageByKeyName(Predicate<String> nameFilter) {
    return onBaggage(
        (incoming, baggage) -> {
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
        });
  }

  /**
   * Creates a processor which appends (exactly) the given attributes.
   *
   * <p>These attributes will not override those attributes provided by instrumentation.
   *
   * @param attributes Attributes to append to measurements.
   */
  public static AttributesProcessor append(Attributes attributes) {
    return simple(incoming -> attributes.toBuilder().putAll(incoming).build());
  }

  /** Creates a simple attributes processor with no access to context. */
  static AttributesProcessor simple(UnaryOperator<Attributes> processor) {
    return new AttributesProcessor() {

      @Override
      public Attributes process(Attributes incoming, Context context) {
        return processor.apply(incoming);
      }

      @Override
      public boolean usesContext() {
        return false;
      }
    };
  }

  /** Creates an Attributes processor that has access to baggage. */
  static AttributesProcessor onBaggage(BiFunction<Attributes, Baggage, Attributes> processor) {
    return new AttributesProcessor() {
      @Override
      public Attributes process(Attributes incoming, Context context) {
        return processor.apply(incoming, Baggage.fromContext(context));
      }

      @Override
      public boolean usesContext() {
        return true;
      }
    };
  }

  /** A {@link AttributesProcessor} that runs a sequence of processors. */
  @Immutable
  static final class JoinedAttributesProcessor extends AttributesProcessor {
    private final Collection<AttributesProcessor> processors;
    private final boolean usesContextCache;

    JoinedAttributesProcessor(Collection<AttributesProcessor> processors) {
      this.processors = processors;
      this.usesContextCache =
          processors.stream().map(AttributesProcessor::usesContext).reduce(false, (l, r) -> l || r);
    }

    @Override
    public Attributes process(Attributes incoming, Context context) {
      Attributes result = incoming;
      for (AttributesProcessor processor : processors) {
        result = processor.process(result, context);
      }
      return result;
    }

    @Override
    public boolean usesContext() {
      return usesContextCache;
    }

    @Override
    public AttributesProcessor then(AttributesProcessor other) {
      List<AttributesProcessor> newList = new ArrayList<>(processors);
      if (other instanceof JoinedAttributesProcessor) {
        newList.addAll(((JoinedAttributesProcessor) other).processors);
      } else {
        newList.add(other);
      }
      return new JoinedAttributesProcessor(newList);
    }

    AttributesProcessor prepend(AttributesProcessor other) {
      List<AttributesProcessor> newList = new ArrayList<>(processors.size() + 1);
      newList.add(other);
      newList.addAll(processors);
      return new JoinedAttributesProcessor(newList);
    }
  }
}
