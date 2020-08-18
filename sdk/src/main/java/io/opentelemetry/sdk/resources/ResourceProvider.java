/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.resources;

import io.opentelemetry.common.Attributes;
import javax.annotation.concurrent.ThreadSafe;

/**
 * ResourceProvider is a service provider for additional {@link Resource}s. Users of OpenTelemetry
 * SDK can use it to add custom {@link Resource} attributes.
 *
 * <p>Fully qualified class name of the implementation should be registered in {@code
 * META-INF/services/io.opentelemetry.sdk.resources.ResourceProvider}.
 *
 * <p>Resources specified via system properties or environment variables will take precedence over
 * any value supplied via {@code ResourceProvider}.
 *
 * @see EnvAutodetectResource
 */
@ThreadSafe
public abstract class ResourceProvider {

  public Resource create() {
    return Resource.create(getAttributes());
  }

  protected abstract Attributes getAttributes();
}
