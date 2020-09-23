/*
 * Copyright The OpenTelemetry Authors
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
/*
 * Copyright 2020 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.opentelemetry.context;

/**
 * The storage for storing and retrieving the current {@link DefaultContext}.
 *
 * <p>If you want to implement your own storage or add some hooks when a {@link DefaultContext} is
 * attached and restored, you should use {@link ContextStorageProvider}. Here's an example that sets
 * MDC before {@link DefaultContext} is attached:
 *
 * <pre>{@code
 * > public class MyStorage implements ContextStorageProvider {
 * >
 * >   @Override
 * >   public ContextStorage get() {
 * >     ContextStorage threadLocalStorage = Context.threadLocalStorage();
 * >     return new RequestContextStorage() {
 * >
 * >       public Context rootContext() {
 * >         // If you need to add mutable state to contexts in your application, you could do
 * >         // something like this instead.
 * >         // return threadLocalStorage.rootContext().withValue(MY_STATE_KEY, new MyState());
 * >         return threadLocalStorage.rootContext();
 * >       }
 * >
 * >       @Nullable
 * >       @Override
 * >       public Context T attach(RequestContext toPush) {
 * >         setMdc(toPush);
 * >         return threadLocalStorage.attach(toPush);
 * >       }
 * >
 * >       @Override
 * >       public void pop(RequestContext current, RequestContext toRestore) {
 * >         clearMdc();
 * >         setMdc(toRestore);
 * >         threadLocalStorage.detach(current, toRestore);
 * >       }
 * >       ...
 * >    }
 * >  }
 * >}
 * }</pre>
 */
public interface ContextStorage {

  /**
   * Returns the {@link ContextStorage} being used by this application. This is only for use when
   * integrating with other context propagation mechanisms and not meant for direct use. To attach
   * or detach a {@link Context} in an application, use {@link Context#attach()} and
   * {@link Scope#close()}.
   */
  static ContextStorage get() {
    return LazyStorage.storage;
  }

  /**
   * Sets the specified {@link Context} as the current {@link Context} and returns a {@link Scope}
   * representing the scope of execution. {@link Scope#close()} must be called when the current
   * {@link Context} should be restored to what it was before attaching {@code toAttach}.
   */
  Scope attach(Context toAttach);

  /**
   * Returns the current {@link DefaultContext}. If no {@link DefaultContext} has been attached yet,
   * this will be the {@linkplain Context#root()} root context}.
   */
  Context current();
}
