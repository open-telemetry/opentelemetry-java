/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;

/**
 * Extended version of {@link DeclarativeConfigProperties} with access to {@link ConfigProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExtendedDeclarativeConfigProperties extends DeclarativeConfigProperties {

  ConfigProvider getConfigProvider();
}
