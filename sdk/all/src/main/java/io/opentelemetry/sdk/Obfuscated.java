/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

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
 */
@ThreadSafe
interface Obfuscated<T> {

  /**
   * Returns the unobfuscated provider.
   *
   * @return the unobfuscated provider.
   */
  T unobfuscate();
}
