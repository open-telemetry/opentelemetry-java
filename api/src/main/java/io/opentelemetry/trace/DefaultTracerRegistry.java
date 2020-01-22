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

package io.opentelemetry.trace;

import com.google.auto.value.AutoValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class DefaultTracerRegistry implements TracerRegistry {

  private final Map<TracerKey, DefaultTracer> tracerRegistry = new HashMap<>();

  private static final DefaultTracerRegistry instance = new DefaultTracerRegistry();

  public static TracerRegistry getInstance() {
    return instance;
  }

  /**
   * Get a map of all the default tracers that were created via this factory.
   *
   * @return A Map of {@link TracerKey} to {@link DefaultTracer}
   */
  public static Map<TracerKey, DefaultTracer> getExistingTracers() {
    synchronized (instance.tracerRegistry) {
      return new HashMap<>(instance.tracerRegistry);
    }
  }

  @Override
  public Tracer get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Tracer get(String instrumentationName, String instrumentationVersion) {
    synchronized (instance.tracerRegistry) {
      TracerKey key = TracerKey.makeKey(instrumentationName, instrumentationVersion);
      DefaultTracer result = tracerRegistry.get(key);
      if (result != null) {
        return result;
      }
      DefaultTracer defaultTracer = new DefaultTracer();
      tracerRegistry.put(key, defaultTracer);
      return defaultTracer;
    }
  }

  @AutoValue
  public abstract static class TracerKey {

    public static TracerKey makeKey(String name, String version) {
      return new AutoValue_DefaultTracerRegistry_TracerKey(name, version);
    }

    public abstract String getName();

    @Nullable
    public abstract String getVersion();
  }
}
