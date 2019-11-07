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

package io.opentelemetry.context.propagation;

import io.opentelemetry.context.Context;

public final class ChainedPropagators {
  public static HttpInjector chain(HttpInjector injector1, HttpInjector injector2) {
    return new ChainedHttpInjector(injector1, injector2);
  }

  public static HttpExtractor chain(HttpExtractor extractor1, HttpExtractor extractor2) {
    return new ChainedHttpExtractor(extractor1, extractor2);
  }

  private ChainedPropagators() {}

  static final class ChainedHttpInjector implements HttpInjector {
    private final HttpInjector injector1;
    private final HttpInjector injector2;

    ChainedHttpInjector(HttpInjector injector1, HttpInjector injector2) {
      this.injector1 = injector1;
      this.injector2 = injector2;
    }

    @Override
    public <C> void inject(Context ctx, C carrier, Setter<C> setter) {
      injector1.inject(ctx, carrier, setter);
      injector2.inject(ctx, carrier, setter);
    }
  }

  static final class ChainedHttpExtractor implements HttpExtractor {
    private final HttpExtractor extractor1;
    private final HttpExtractor extractor2;

    ChainedHttpExtractor(HttpExtractor extractor1, HttpExtractor extractor2) {
      this.extractor1 = extractor1;
      this.extractor2 = extractor2;
    }

    @Override
    public <C> Context extract(Context ctx, C carrier, Getter<C> getter) {
      ctx = extractor1.extract(ctx, carrier, getter);
      ctx = extractor2.extract(ctx, carrier, getter);
      return ctx;
    }
  }
}
