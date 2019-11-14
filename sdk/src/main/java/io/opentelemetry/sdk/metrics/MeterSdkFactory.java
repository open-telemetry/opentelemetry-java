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

import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code Meter} provider implementation for {@link MeterFactory}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.OpenTelemetry}.
 */
public class MeterSdkFactory implements MeterFactory {
  private final Map<String, Meter> metersByKey =
      Collections.synchronizedMap(new HashMap<String, Meter>());

  @Override
  public Meter get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Meter get(String instrumentationName, String instrumentationVersion) {
    String key = instrumentationName + "/" + instrumentationVersion;
    Meter meter = metersByKey.get(key);

    if (meter == null) {
      meter = new MeterSdk();
      metersByKey.put(key, meter);
    }

    return meter;
  }
}
