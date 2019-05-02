/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opencensusshim.trace;

import io.opentelemetry.opencensusshim.common.Clock;
import io.opentelemetry.opencensusshim.internal.DefaultVisibilityForTesting;
import io.opentelemetry.opencensusshim.internal.Provider;
import io.opentelemetry.opencensusshim.trace.config.TraceConfig;
import io.opentelemetry.opencensusshim.trace.export.ExportComponent;
import io.opentelemetry.opencensusshim.trace.propagation.PropagationComponent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Class that manages a global instance of the {@link TraceComponent}.
 *
 * @since 0.1.0
 */
public final class Tracing {
  private static final Logger logger = Logger.getLogger(Tracing.class.getName());
  private static final TraceComponent traceComponent =
      loadTraceComponent(TraceComponent.class.getClassLoader());

  /**
   * Returns the global {@link Tracer}.
   *
   * @return the global {@code Tracer}.
   * @since 0.1.0
   */
  public static Tracer getTracer() {
    return traceComponent.getTracer();
  }

  /**
   * Returns the global {@link PropagationComponent}.
   *
   * @return the global {@code PropagationComponent}.
   * @since 0.1.0
   */
  public static PropagationComponent getPropagationComponent() {
    return traceComponent.getPropagationComponent();
  }

  /**
   * Returns the global {@link Clock}.
   *
   * @return the global {@code Clock}.
   * @since 0.1.0
   */
  public static Clock getClock() {
    return traceComponent.getClock();
  }

  /**
   * Returns the global {@link ExportComponent}.
   *
   * @return the global {@code ExportComponent}.
   * @since 0.1.0
   */
  public static ExportComponent getExportComponent() {
    return traceComponent.getExportComponent();
  }

  /**
   * Returns the global {@link TraceConfig}.
   *
   * @return the global {@code TraceConfig}.
   * @since 0.1.0
   */
  public static TraceConfig getTraceConfig() {
    return traceComponent.getTraceConfig();
  }

  // Any provider that may be used for TraceComponent can be added here.
  @DefaultVisibilityForTesting
  static TraceComponent loadTraceComponent(@Nullable ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "opentelemetry.opencensusshim.impl.trace.TraceComponentImpl",
              /*initialize=*/ true,
              classLoader),
          TraceComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load full implementation for TraceComponent, now trying to load lite "
              + "implementation.",
          e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "opentelemetry.opencensusshim.impllite.trace.TraceComponentImplLite",
              /*initialize=*/ true,
              classLoader),
          TraceComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for TraceComponent, now using "
              + "default implementation for TraceComponent.",
          e);
    }
    return TraceComponent.newNoopTraceComponent();
  }

  // No instance of this class.
  private Tracing() {}
}
