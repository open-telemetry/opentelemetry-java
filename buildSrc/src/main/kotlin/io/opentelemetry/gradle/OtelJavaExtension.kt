/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.provider.Property

abstract class OtelJavaExtension {
    abstract val moduleName: Property<String>

    abstract val minJavaVersionSupported: Property<JavaVersion>

    init {
        minJavaVersionSupported.convention(JavaVersion.VERSION_1_8)
    }
}
