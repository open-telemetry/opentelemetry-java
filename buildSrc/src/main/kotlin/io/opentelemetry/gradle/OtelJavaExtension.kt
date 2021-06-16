/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle

import org.gradle.api.provider.Property

abstract class OtelJavaExtension {
    abstract val moduleName: Property<String>
}
