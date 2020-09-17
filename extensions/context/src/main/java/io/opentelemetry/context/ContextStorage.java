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
   * Returns the root context from which all other contexts are derived from. The default {@link
   * ContextStorage} provides an empty root context, but this method can be overridden to set values
   * into the root context, which will be available in all descendant {@link Context}s.
   */
  Context rootContext();

  /**
   * Sets the specified {@link Context} into the storage.
   *
   * @return the old {@link Context} which was in the storage before the specified {@code toPush} is
   *     pushed. {@code null}, if there was no {@link Context} previously.
   */
  Context attach(Context toPush);

  /**
   * Removes the {@code current} {@link Context} from the storage and sets back the specified {@code
   * toRestore}. {@code toRestore} is the {@link Context} returned from the call to {@link
   * #attach(Context)} which set {@code current}.
   *
   * <p>The specified {@code current} must be the current {@link Context} in the storage. If it is
   * not, it means that {@link Scope#close()} was not called properly and the current state of the
   * context is invalid. A warning will be logged for this case.
   */
  void detach(Context current, Context toRestore);

  /**
   * Returns the current {@link Context}. If no {@link Context} has been attached yet, this will be
   * the {@linkplain #rootContext() root context}.
   */
  Context current();
}
