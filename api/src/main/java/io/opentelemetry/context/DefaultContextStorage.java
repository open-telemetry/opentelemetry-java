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

package io.opentelemetry.context;

import java.util.logging.Level;
import java.util.logging.Logger;

// TODO (trask) citation needed, copied from grpc-context
//      if needed, we can make this storage pluggable similar to grpc-context
final class DefaultContextStorage {
  private static final Logger log = Logger.getLogger(DefaultContextStorage.class.getName());

  /** Currently bound context. */
  static final ThreadLocal<Context> localContext = new ThreadLocal<>();

  static Context doAttach(Context toAttach) {
    Context current = current();
    localContext.set(toAttach);
    return current;
  }

  static void detach(Context toDetach, Context toRestore) {
    if (current() != toDetach) {
      // Log a severe message instead of throwing an exception as the context to attach is assumed
      // to be the correct one and the unbalanced state represents a coding mistake in a lower
      // layer in the stack that cannot be recovered from here.
      log.log(
          Level.SEVERE,
          "Context was not attached when detaching",
          new Throwable().fillInStackTrace());
    }
    if (toRestore != Context.empty()) {
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
  }

  static Context current() {
    Context current = localContext.get();
    if (current == null) {
      return DefaultContext.EMPTY;
    }
    return current;
  }

  private DefaultContextStorage() {}
}
