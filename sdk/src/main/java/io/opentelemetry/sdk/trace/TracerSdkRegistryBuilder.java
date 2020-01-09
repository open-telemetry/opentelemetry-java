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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.EnvVarResource;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nonnull;

/**
 * Builder class for the TracerSdkFactory. Has fully functional default implementations of all three
 * required interfaces.
 *
 * @since 0.4.0
 */
public class TracerSdkRegistryBuilder {

  private Clock clock = MillisClock.getInstance();
  private IdsGenerator idsGenerator = new RandomIdsGenerator();
  private Resource resource = EnvVarResource.getResource();

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public TracerSdkRegistryBuilder setClock(@Nonnull Clock clock) {
    Utils.checkNotNull(clock, "The Clock must be non-null");
    this.clock = clock;
    return this;
  }

  /**
   * Assign an {@link IdsGenerator}.
   *
   * @param idsGenerator A generator for trace and span ids. Note: this should be thread-safe and as
   *     contention free as possible.
   * @return this
   */
  public TracerSdkRegistryBuilder setIdsGenerator(@Nonnull IdsGenerator idsGenerator) {
    Utils.checkNotNull(idsGenerator, "The IdsGenerator must be non-null");
    this.idsGenerator = idsGenerator;
    return this;
  }

  /**
   * Assign a {@link Resource} to be attached to all Spans created by Tracers.
   *
   * @param resource A Resource implementation.
   * @return this
   */
  public TracerSdkRegistryBuilder setResource(@Nonnull Resource resource) {
    Utils.checkNotNull(resource, "The Resource must be non-null");
    this.resource = resource;
    return this;
  }

  /**
   * Create a new TracerSdkFactory instance.
   *
   * @return An initialized TracerSdkFactory.
   */
  public TracerSdkRegistry build() {
    return new TracerSdkRegistry(clock, idsGenerator, resource);
  }
}
