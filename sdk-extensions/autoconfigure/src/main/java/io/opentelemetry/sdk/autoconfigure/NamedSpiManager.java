/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import javax.annotation.Nullable;

interface NamedSpiManager<T> {

  static <T> NamedSpiManager<T> emptyManager() {
    return name -> null;
  }

  @Nullable
  T getByName(String name);
}
