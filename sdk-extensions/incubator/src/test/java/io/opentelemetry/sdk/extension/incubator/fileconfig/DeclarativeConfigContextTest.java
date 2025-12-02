/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class DeclarativeConfigContextTest {

  private final DeclarativeConfigContext context =
      DeclarativeConfigContext.create(
          ComponentLoader.forClassLoader(DeclarativeConfigContextTest.class.getClassLoader()));

  @Test
  void resourceMustBeSetBeforeUse() {
    assertThatCode(context::getResource).isInstanceOf(DeclarativeConfigException.class);
    Resource resource = Resource.empty();
    context.setResource(resource);
    assertThat(context.getResource()).isSameAs(resource);
  }
}
