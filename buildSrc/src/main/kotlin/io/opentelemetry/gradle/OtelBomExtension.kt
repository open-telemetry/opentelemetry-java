/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.util.function.Predicate

abstract class OtelBomExtension {
  abstract val projectFilter: Property<Predicate<Project>>
  val additionalDependencies: MutableSet<String> = hashSetOf()

  fun addFallback(artifactId: String, version: String) {
    this.additionalDependencies.add("io.opentelemetry:" + artifactId + ":" + version)
  }

  fun addExtra(groupId: String, artifactId: String, version: String) {
    this.additionalDependencies.add(groupId + ":" + artifactId + ":" + version)
  }
}
