/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfiguredComponentWrapper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Wraps components with SPI implementations.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ComponentWrapping {

  private static final List<AutoConfiguredComponentWrapper> wrappers;

  static {
    List<AutoConfiguredComponentWrapper> loaded = new ArrayList<>();
    ServiceLoader.load(AutoConfiguredComponentWrapper.class).forEach(loaded::add);
    wrappers = Collections.unmodifiableList(loaded);
  }

  /** Wraps a {@link Resource}. */
  public static Resource wrap(Resource resource, ConfigProperties config) {
    for (AutoConfiguredComponentWrapper wrapper : wrappers) {
      resource = wrapper.wrap(resource, config);
    }
    return resource;
  }

  /** Wraps a {@link Sampler}. */
  public static Sampler wrap(Sampler sampler, ConfigProperties config) {
    for (AutoConfiguredComponentWrapper wrapper : wrappers) {
      sampler = wrapper.wrap(sampler, config);
    }
    return sampler;
  }

  /**
   * Wraps a {@link SpanExporter}. It is common to use in conjunction with {@link
   * io.opentelemetry.sdk.trace.data.DelegatingSpanData} to adjust the exported data.
   */
  public static SpanExporter wrap(SpanExporter exporter, ConfigProperties config) {
    for (AutoConfiguredComponentWrapper wrapper : wrappers) {
      exporter = wrapper.wrap(exporter, config);
    }
    return exporter;
  }

  /** Wraps a {@link TextMapPropagator}. */
  public static TextMapPropagator wrap(TextMapPropagator propagator, ConfigProperties config) {
    for (AutoConfiguredComponentWrapper wrapper : wrappers) {
      propagator = wrapper.wrap(propagator, config);
    }
    return propagator;
  }

  private ComponentWrapping() {}
}
