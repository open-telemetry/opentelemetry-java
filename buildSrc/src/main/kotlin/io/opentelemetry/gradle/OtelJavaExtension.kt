/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class OtelJavaExtension {
    abstract val moduleName: Property<String>

    // Set to false for modules that are not OSGi bundles (e.g. test helpers, build tooling,
    // aggregators). Skips BND bundle metadata generation entirely.
    abstract val osgiEnabled: Property<Boolean>

    abstract val osgiOptionalPackages: ListProperty<String>

    // Packages accessed via Class.forName that are not on the compile classpath (e.g. due to
    // circular dependencies). These are excluded from Import-Package and added to
    // DynamicImport-Package so OSGi does not require them at resolution time, but can still wire
    // them at runtime when available.
    abstract val osgiDynamicImportPackages: ListProperty<String>

    abstract val minJavaVersionSupported: Property<JavaVersion>

    init {
        minJavaVersionSupported.convention(JavaVersion.VERSION_1_8)
        osgiEnabled.convention(true)
        osgiOptionalPackages.convention(emptyList<String>())
        osgiDynamicImportPackages.convention(emptyList<String>())
    }
}
