/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.util.function.Predicate

abstract class OtelBomExtension {
  abstract val projectFilter: Property<Predicate<Project>>
  val fallbacks: MutableSet<String> = hashSetOf()

  fun addFallback(artifactId: String, version: String) {
    this.fallbacks.add("io.opentelemetry:" + artifactId + ":" + version)
  }
}