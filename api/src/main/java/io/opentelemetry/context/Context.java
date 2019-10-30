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

package io.opentelemetry.context;

public final class Context {
  private final io.grpc.Context ctx;

  private Context(io.grpc.Context ctx) {
    this.ctx = ctx;
  }

  public static Context current() {
    return new Context(io.grpc.Context.current());
  }

  public static Scope setCurrent(Context ctx) {
    return new ScopeImpl(ctx);
  }

  public static <T> Context.Key<T> createKey(String name) {
    return new Key<T>(io.grpc.Context.<T>key(name));
  }

  public <T> T getValue(Context.Key<T> key) {
    return key.key().get(ctx);
  }

  public <T> Context setValue(Context.Key<T> key, T value) {
    return new Context(ctx.withValue(key.key(), value));
  }

  public static final class Key<T> {
    io.grpc.Context.Key<T> key;

    private Key(io.grpc.Context.Key<T> key) {
      this.key = key;
    }

    io.grpc.Context.Key<T> key() {
      return key;
    }
  }

  static final class ScopeImpl implements Scope {
    private final io.grpc.Context ctx;
    private final io.grpc.Context prevCtx;

    public ScopeImpl(Context ctx) {
      this.ctx = ctx.ctx;
      this.prevCtx = ctx.ctx.attach();
    }

    @Override
    public void close() {
      ctx.detach(prevCtx);
    }
  }
}
