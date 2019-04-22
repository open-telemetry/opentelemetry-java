/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.opencensusshim.trace;

import openconsensus.opencensusshim.common.Clock;
import openconsensus.opencensusshim.internal.ZeroTimeClock;
import openconsensus.opencensusshim.trace.config.TraceConfig;
import openconsensus.opencensusshim.trace.export.ExportComponent;
import openconsensus.opencensusshim.trace.propagation.PropagationComponent;

/**
 * Class that holds the implementation instances for {@link Tracer}, {@link PropagationComponent},
 * {@link Clock}, {@link ExportComponent} and {@link TraceConfig}.
 *
 * <p>Unless otherwise noted all methods (on component) results are cacheable.
 *
 * @since 0.1.0
 */
public abstract class TraceComponent {

  /**
   * Returns the {@link Tracer} with the provided implementations. If no implementation is provided
   * then no-op implementations will be used.
   *
   * @return the {@code Tracer} implementation.
   * @since 0.1.0
   */
  public abstract Tracer getTracer();

  /**
   * Returns the {@link PropagationComponent} with the provided implementation. If no implementation
   * is provided then no-op implementation will be used.
   *
   * @return the {@code PropagationComponent} implementation.
   * @since 0.1.0
   */
  public abstract PropagationComponent getPropagationComponent();

  /**
   * Returns the {@link Clock} with the provided implementation.
   *
   * @return the {@code Clock} implementation.
   * @since 0.1.0
   */
  public abstract Clock getClock();

  /**
   * Returns the {@link ExportComponent} with the provided implementation. If no implementation is
   * provided then no-op implementations will be used.
   *
   * @return the {@link ExportComponent} implementation.
   * @since 0.1.0
   */
  public abstract ExportComponent getExportComponent();

  /**
   * Returns the {@link TraceConfig} with the provided implementation. If no implementation is
   * provided then no-op implementations will be used.
   *
   * @return the {@link TraceConfig} implementation.
   * @since 0.1.0
   */
  public abstract TraceConfig getTraceConfig();

  /**
   * Returns an instance that contains no-op implementations for all the instances.
   *
   * @return an instance that contains no-op implementations for all the instances.
   */
  static TraceComponent newNoopTraceComponent() {
    return new NoopTraceComponent();
  }

  private static final class NoopTraceComponent extends TraceComponent {
    private final ExportComponent noopExportComponent = ExportComponent.newNoopExportComponent();

    @Override
    public Tracer getTracer() {
      return Tracer.getNoopTracer();
    }

    @Override
    public PropagationComponent getPropagationComponent() {
      return PropagationComponent.getNoopPropagationComponent();
    }

    @Override
    public Clock getClock() {
      return ZeroTimeClock.getInstance();
    }

    @Override
    public ExportComponent getExportComponent() {
      return noopExportComponent;
    }

    @Override
    public TraceConfig getTraceConfig() {
      return TraceConfig.getNoopTraceConfig();
    }

    private NoopTraceComponent() {}
  }
}
