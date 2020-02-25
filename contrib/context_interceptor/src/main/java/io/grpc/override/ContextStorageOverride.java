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

package io.grpc.override;

import io.grpc.Context;
import io.opentelemetry.contrib.context.interceptor.Interceptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ContextStorageOverride} is a copy of the grpc ThreadLocalContextStorage with the ability
 * to add interceptors for every Context change.
 */
public final class ContextStorageOverride extends Context.Storage {
  private static final Logger log = Logger.getLogger(ContextStorageOverride.class.getName());
  private static volatile List<Interceptor> interceptors = Collections.emptyList();

  /** Public constructor to be loaded by the {@link Context}. */
  public ContextStorageOverride() {}

  /**
   * Adds a new interceptor that is called every Context update.
   *
   * @param interceptor the {@code Interceptor} to be called.
   */
  public static synchronized void addInterceptor(Interceptor interceptor) {
    ArrayList<Interceptor> newInterceptors = new ArrayList<>(interceptors.size() + 1);
    newInterceptors.addAll(interceptors);
    newInterceptors.add(interceptor);
    interceptors = newInterceptors;
  }

  /** Currently bound context. */
  // VisibleForTesting
  static final ThreadLocal<Context> localContext = new ThreadLocal<>();

  @Override
  public Context doAttach(Context toAttach) {
    Context current = current();
    localContext.set(toAttach);
    for (Interceptor interceptor : interceptors) {
      interceptor.updated(current, toAttach);
    }
    return current;
  }

  @Override
  public void detach(Context toDetach, Context toRestore) {
    if (current() != toDetach) {
      // Log a severe message instead of throwing an exception as the context to attach is assumed
      // to be the correct one and the unbalanced state represents a coding mistake in a lower
      // layer in the stack that cannot be recovered from here.
      log.log(
          Level.SEVERE,
          "Context was not attached when detaching",
          new Throwable().fillInStackTrace());
    }
    if (toRestore != Context.ROOT) {
      localContext.set(toRestore);
    } else {
      // Avoid leaking our ClassLoader via ROOT if this Thread is reused across multiple
      // ClassLoaders, as is common for Servlet Containers. The ThreadLocal is weakly referenced by
      // the Thread, but its current value is strongly referenced and only lazily collected as new
      // ThreadLocals are created.
      //
      // Use set(null) instead of remove() since remove() deletes the entry which is then re-created
      // on the next get() (because of initialValue() handling). set(null) has same performance as
      // set(toRestore).
      localContext.set(null);
    }
    for (Interceptor interceptor : interceptors) {
      interceptor.updated(toDetach, toRestore);
    }
  }

  @Override
  public Context current() {
    Context current = localContext.get();
    if (current == null) {
      return Context.ROOT;
    }
    return current;
  }
}
