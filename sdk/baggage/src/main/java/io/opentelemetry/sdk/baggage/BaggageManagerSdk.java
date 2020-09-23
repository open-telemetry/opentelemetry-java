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

package io.opentelemetry.sdk.baggage;

import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.context.Scope;

/** {@link BaggageManagerSdk} is SDK implementation of {@link BaggageManager}. */
public class BaggageManagerSdk implements BaggageManager {

  @Override
  public Baggage getCurrentBaggage() {
    return BaggageUtils.getCurrentBaggage();
  }

  @Override
  public Baggage.Builder contextBuilder() {
    return new BaggageSdk.Builder();
  }

  @Override
  public Scope withContext(Baggage distContext) {
    return BaggageUtils.currentContextWith(distContext);
  }
}
