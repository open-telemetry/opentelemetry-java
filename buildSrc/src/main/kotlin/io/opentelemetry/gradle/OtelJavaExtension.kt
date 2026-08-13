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

    // SPI interfaces whose implementations this bundle registers via META-INF/services.
    // Generates Provide-Capability: osgi.serviceloader;... and requires the registrar extender.
    abstract val osgiServiceLoaderProvides: ListProperty<String>

    // SPI interfaces this bundle discovers at runtime via ServiceLoader.
    // Generates Require-Capability: osgi.serviceloader;... (resolution:=optional) to hint the
    // BND resolver to include provider bundles. Does NOT add the processor extender requirement —
    // use osgiServiceLoaderProcessor for that.
    abstract val osgiServiceLoaderRequires: ListProperty<String>

    // When true, adds Require-Capability: osgi.extender=osgi.serviceloader.processor so that a
    // ServiceLoader mediator (e.g. SPI Fly) weaves this bundle's ServiceLoader.load() calls to
    // route through the OSGi service registry. Set this on whichever bundle contains the actual
    // ServiceLoader.load() call site; the SPI types being loaded live elsewhere.
    abstract val osgiServiceLoaderProcessor: Property<Boolean>

    abstract val minJavaVersionSupported: Property<JavaVersion>

    init {
        minJavaVersionSupported.convention(JavaVersion.VERSION_1_8)
        osgiEnabled.convention(true)
        osgiOptionalPackages.convention(emptyList())
        osgiUnversionedOptionalPackages.convention(emptyList())
        osgiServiceLoaderProvides.convention(emptyList())
        osgiServiceLoaderRequires.convention(emptyList())
        osgiServiceLoaderProcessor.convention(false)
    }
}
