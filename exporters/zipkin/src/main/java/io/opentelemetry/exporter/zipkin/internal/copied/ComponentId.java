/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal.copied;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

/**
 * The component id used for SDK health metrics. This corresponds to the otel.component.name and
 * otel.component.id semconv attributes.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class ComponentId {

  private ComponentId() {}

  public abstract String getTypeName();

  public abstract String getComponentName();

  static class Lazy extends ComponentId {

    private static final Map<String, AtomicInteger> nextIdCounters = new ConcurrentHashMap<>();

    private final String componentType;
    @Nullable private volatile String componentName = null;

    Lazy(String componentType) {
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

  public static StandardComponentId generateLazy(StandardComponentId.ExporterType exporterType) {
    return new StandardComponentId(exporterType);
  }
}
