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

package io.opentelemetry.internal;

import javax.annotation.concurrent.ThreadSafe;

/**
 * This interface allows the SDK to unobfuscate an obfuscated static global provider.
 *
 * <p>Static global providers are obfuscated when they are returned from the API to prevent users
 * from casting them to their SDK specific implementation.
 *
 * <p>This is important for auto-instrumentation, because if users take the static global providers
 * that are returned from the API, and cast them to their SDK specific implementations, then those
 * casts will fail under auto-instrumentation, because auto-instrumentation takes over the static
 * global providers returned by the API and points them to it's embedded SDK.
 *
 * @since 0.4.0
 */
@ThreadSafe
public interface Obfuscated {

  /**
   * Returns the unobfuscated provider.
   *
   * @return the unobfuscated provider.
   * @since 0.4.0
   */
  Object unobfuscate();
}
