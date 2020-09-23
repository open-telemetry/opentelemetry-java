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

package io.opentelemetry.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Context;
import org.junit.jupiter.api.Test;

class BaggageUtilsTest {

  @Test
  void testGetCurrentBaggage_Default() {
    Baggage baggage = BaggageUtils.getCurrentBaggage();
    assertThat(baggage).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void testGetCurrentBaggage_SetCorrContext() {
    Baggage baggage = DefaultBaggageManager.getInstance().contextBuilder().build();
    Context orig = BaggageUtils.withBaggage(baggage, Context.current()).attach();
    try {
      assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(baggage);
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  void testGetBaggage_DefaultContext() {
    Baggage baggage = BaggageUtils.getBaggage(Context.current());
    assertThat(baggage).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void testGetBaggage_ExplicitContext() {
    Baggage baggage = DefaultBaggageManager.getInstance().contextBuilder().build();
    Context context = BaggageUtils.withBaggage(baggage, Context.current());
    assertThat(BaggageUtils.getBaggage(context)).isSameAs(baggage);
  }

  @Test
  void testGetBaggageWithoutDefault_DefaultContext() {
    Baggage baggage = BaggageUtils.getBaggageWithoutDefault(Context.current());
    assertThat(baggage).isNull();
  }

  @Test
  void testGetBaggageWithoutDefault_ExplicitContext() {
    Baggage baggage = DefaultBaggageManager.getInstance().contextBuilder().build();
    Context context = BaggageUtils.withBaggage(baggage, Context.current());
    assertThat(BaggageUtils.getBaggage(context)).isSameAs(baggage);
  }
}
