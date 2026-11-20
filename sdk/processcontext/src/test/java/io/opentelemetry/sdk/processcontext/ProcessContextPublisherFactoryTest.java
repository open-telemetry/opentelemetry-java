/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProcessContextPublisherFactoryTest {

  @Test
  void noopByDefault() {

    ProcessContextPublisher processContextPublisher =
        ProcessContextPublisherFactory.getInstance().create();

    assertThat(processContextPublisher).isNotNull();
    assertThat(processContextPublisher).isInstanceOf(NoopProcessContextPublisher.class);
  }
}
