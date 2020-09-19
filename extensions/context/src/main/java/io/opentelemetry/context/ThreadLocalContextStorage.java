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
package io.opentelemetry.context;

import java.util.logging.Level;
import java.util.logging.Logger;

enum ThreadLocalContextStorage implements ContextStorage {
  INSTANCE;

  private static final Logger logger = Logger.getLogger(ThreadLocalContextStorage.class.getName());

  private static final ThreadLocal<Context> THREAD_LOCAL_STORAGE = new ThreadLocal<>();

  @Override
  public Context attach(Context toPush) {
    Context current = current();
    // Null context not allowed so ignore it.
    if (toPush != null) {
      THREAD_LOCAL_STORAGE.set(toPush);
    }
    return current;
  }

  @Override
  public void detach(Context current, Context toRestore) {
    Context stored = current();
    if (stored != current) {
      logger.log(
          Level.FINE,
          "Context in storage not the expected context, Scope.close was not called correctly");
    }
    THREAD_LOCAL_STORAGE.set(toRestore);
  }

  @Override
  public Context current() {
    return THREAD_LOCAL_STORAGE.get();
  }
}
