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
import java.util.List;

final class ContextStorageWithListeners extends Context.Storage {
  private final Context.Storage contextStorageImpl;
  private final List<ContextStorageListener> contextStorageListeners;

  ContextStorageWithListeners(
      Context.Storage contextStorageImpl, List<ContextStorageListener> contextStorageListeners) {
    this.contextStorageImpl = contextStorageImpl;
    this.contextStorageListeners = contextStorageListeners;
  }

  @Override
  public Context doAttach(Context toAttach) {
    Context detached = contextStorageImpl.doAttach(toAttach);
    for (ContextStorageListener contextStorageListener : contextStorageListeners) {
      contextStorageListener.contextUpdated(detached, toAttach);
    }
    return detached;
  }

  @Override
  public void detach(Context toDetach, Context toRestore) {
    contextStorageImpl.detach(toDetach, toRestore);
    for (ContextStorageListener contextStorageListener : contextStorageListeners) {
      contextStorageListener.contextUpdated(toDetach, toRestore);
    }
  }

  @Override
  public Context current() {
    return contextStorageImpl.current();
  }
}
