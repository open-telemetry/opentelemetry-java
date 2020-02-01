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

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.MeterRegistry;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.EnvVarResource;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nonnull;

/**
 * {@code Meter} provider implementation for {@link MeterRegistry}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.OpenTelemetry}.
 */
public final class MeterSdkRegistry implements MeterRegistry {
  private final MeterSharedState sharedState;
  private final MeterSdkComponentRegistry registry;

  private MeterSdkRegistry(Clock clock, Resource resource) {
    this.sharedState = new MeterSharedState(clock, resource);
    this.registry = new MeterSdkComponentRegistry(sharedState);
  }

  @Override
  public MeterSdk get(String instrumentationName) {
    return registry.get(instrumentationName);
  }

  @Override
  public MeterSdk get(String instrumentationName, String instrumentationVersion) {
    return registry.get(instrumentationName, instrumentationVersion);
  }

  /**
   * Returns a new {@link Builder} for {@link MeterSdkRegistry}.
   *
   * @return a new {@link Builder} for {@link MeterSdkRegistry}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for the {@link MeterSdkRegistry}. Has fully functional default implementations of
   * all three required interfaces.
   *
   * @since 0.4.0
   */
  public static final class Builder {

    private Clock clock = MillisClock.getInstance();
    private Resource resource = EnvVarResource.getResource();

    private Builder() {}

    /**
     * Assign a {@link Clock}.
     *
     * @param clock The clock to use for all temporal needs.
     * @return this
     */
    public Builder setClock(@Nonnull Clock clock) {
      Utils.checkNotNull(clock, "clock");
      this.clock = clock;
      return this;
    }

    /**
     * Assign a {@link Resource} to be attached to all Spans created by Tracers.
     *
     * @param resource A Resource implementation.
     * @return this
     */
    public Builder setResource(@Nonnull Resource resource) {
      Utils.checkNotNull(resource, "resource");
      this.resource = resource;
      return this;
    }

    /**
     * Create a new TracerSdkFactory instance.
     *
     * @return An initialized TracerSdkFactory.
     */
    public MeterSdkRegistry build() {
      return new MeterSdkRegistry(clock, resource);
    }
  }

  private static final class MeterSdkComponentRegistry extends ComponentRegistry<MeterSdk> {
    private final MeterSharedState sharedState;

    private MeterSdkComponentRegistry(MeterSharedState sharedState) {
      this.sharedState = sharedState;
    }

    @Override
    public MeterSdk newComponent(InstrumentationLibraryInfo instrumentationLibraryInfo) {
      return new MeterSdk(sharedState, instrumentationLibraryInfo);
    }
  }
}
