/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
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
 * The storage for storing and retrieving the current {@link Context}.
 *
 * <p>If you want to implement your own storage or add some hooks when a {@link Context} is attached
 * and restored, you should use {@link ContextStorageProvider}. Here's an example that sets MDC
 * before {@link Context} is attached:
 *
 * <pre>{@code
 * > public class MyStorage implements ContextStorageProvider {
 * >
 * >   @Override
 * >   public ContextStorage get() {
 * >     ContextStorage threadLocalStorage = Context.threadLocalStorage();
 * >     return new RequestContextStorage() {
 * >       @Override
 * >       public Scope T attach(Context toAttach) {
 * >         Context current = current();
 * >         setMdc(toAttach);
 * >         Scope scope = threadLocalStorage.attach(toAttach);
 * >         return () -> {
 * >           clearMdc();
 * >           setMdc(current);
 * >           scope.close();
 * >         }
 * >       }
 * >
 * >       @Override
 * >       public Context current() {
 * >         return threadLocalStorage.current();
 * >       }
 * >     }
 * >   }
 * > }
 * }</pre>
 */
public interface ContextStorage {

  /**
   * Returns the {@link ContextStorage} being used by this application. This is only for use when
   * integrating with other context propagation mechanisms and not meant for direct use. To attach
   * or detach a {@link Context} in an application, use {@link Context#makeCurrent()} and {@link
   * Scope#close()}.
   */
  static ContextStorage get() {
    return LazyStorage.get();
  }

  /**
   * Sets the {@link ContextStorage} being used by this application to the provided {@link
   * ContextStorage}. This must be called as early as possible in the application or some {@link
   * Context} may use the wrong storage, for example, as part of a static initialization block in
   * your main class. To avoid ordering issues, for non-testing situations it is recommended to use
   * {@link ContextStorageProvider} instead of this method.
   */
  static void set(ContextStorage storage) {
    LazyStorage.set(storage);
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
