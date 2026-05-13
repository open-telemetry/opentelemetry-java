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

    // Packages that should be optional imports but are not on the compile classpath (e.g. due to
    // circular dependencies), so BND cannot resolve version="${@}" for them. Added to Import-Package
    // with resolution:=optional but no version constraint.
    abstract val osgiUnversionedOptionalPackages: ListProperty<String>

    abstract val minJavaVersionSupported: Property<JavaVersion>

    init {
        minJavaVersionSupported.convention(JavaVersion.VERSION_1_8)
        osgiEnabled.convention(true)
        osgiOptionalPackages.convention(emptyList<String>())
        osgiUnversionedOptionalPackages.convention(emptyList<String>())
    }
}
