/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The component id used for SDK health metrics. This corresponds to the otel.component.name and
 * otel.component.id semconv attributes.
 */
public abstract class ComponentId {

  // TODO: add tests against semconv
  private static final AttributeKey<String> OTEL_COMPONENT_TYPE =
      AttributeKey.stringKey("otel.component.type");
  private static final AttributeKey<String> OTEL_COMPONENT_NAME =
      AttributeKey.stringKey("otel.component.name");

  private ComponentId() {}

  public abstract String getTypeName();

  public abstract void put(AttributesBuilder attributes);

  private static class Impl extends ComponentId {
    private static final Map<String, AtomicInteger> nextIdCounters = new ConcurrentHashMap<>();

    private final String componentType;
    private final String componentName;

    private Impl(String componentType) {
      int id =
          nextIdCounters
              .computeIfAbsent(componentType, k -> new AtomicInteger(0))
              .getAndIncrement();
      this.componentType = componentType;
      componentName = componentType + "/" + id;
    }

    @Override
    public String getTypeName() {
      return componentType;
    }

    @Override
    public void put(AttributesBuilder attributes) {
      attributes.put(OTEL_COMPONENT_TYPE, componentType);
      attributes.put(OTEL_COMPONENT_NAME, componentName);
    }
  }

  private static class Lazy extends ComponentId {

    private final String componentType;
    private volatile Impl delegate = null;

    private Lazy(String componentType) {
      this.componentType = componentType;
    }

    @Override
    public String getTypeName() {
      return componentType;
    }

    @Override
    public void put(AttributesBuilder attributes) {
      if (delegate == null) {
        synchronized (this) {
          if (delegate == null) {
            delegate = new Impl(componentType);
          }
        }
      }
      delegate.put(attributes);
    }
  }

  public static ComponentId generateLazy(String componentType) {
    return new Lazy(componentType);
  }
}
