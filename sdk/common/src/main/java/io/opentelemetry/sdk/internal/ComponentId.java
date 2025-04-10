/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.AttributesBuilder;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The component id used for SDK health metrics. This corresponds to the otel.component.name and
 * otel.component.id semconv attributes.
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public abstract class ComponentId {

  private ComponentId() {}

  public abstract String getTypeName();

  public abstract String getComponentName();

  public void put(AttributesBuilder attributes) {
    attributes.put(SemConvAttributes.OTEL_COMPONENT_TYPE, getTypeName());
    attributes.put(SemConvAttributes.OTEL_COMPONENT_NAME, getComponentName());
  }

  private static class Lazy extends ComponentId {

    private static final Map<String, AtomicInteger> nextIdCounters = new ConcurrentHashMap<>();

    private final String componentType;
    @Nullable
    private volatile String componentName = null;

    private Lazy(String componentType) {
      this.componentType = componentType;
    }

    @Override
    public String getTypeName() {
      return componentType;
    }

    @Override
    public String getComponentName() {
      if (componentName == null) {
        synchronized (this) {
          if (componentName == null) {
            int id =
                nextIdCounters
                    .computeIfAbsent(componentType, k -> new AtomicInteger(0))
                    .getAndIncrement();
            componentName = componentType + "/" + id;
          }
        }
      }
      return componentName;
    }


  }

  public static ComponentId generateLazy(String componentType) {
    return new Lazy(componentType);
  }
}
