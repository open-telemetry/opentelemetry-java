/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtests.graal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.trace.internal.JcTools;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

class JcToolsTest {

  @Test
  @EnabledInNativeImage
  void insert_oneElementToTheQueue() {
    Queue<Object> objects = JcTools.newFixedSizeQueue(10);

    objects.add(new Object());

    assertThat(objects).hasSize(1);
  }
}
