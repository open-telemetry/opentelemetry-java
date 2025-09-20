/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public interface ComponentProviderLoader {
  <T> T loadComponent(Class<T> type, String name, DeclarativeConfigProperties properties);
}
