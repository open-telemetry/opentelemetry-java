/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.StatusCode;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ImmutableStatusData}. */
class ImmutableStatusDataTest {
  @Test
  void defaultConstants() {
    StatusCode[] codes = StatusCode.values();
    assertThat(codes).hasSize(3);
    assertThat(StatusData.unset().getStatusCode()).isEqualTo(StatusCode.UNSET);
    assertThat(StatusData.unset().getDescription()).isEmpty();
    assertThat(StatusData.ok().getStatusCode()).isEqualTo(StatusCode.OK);
    assertThat(StatusData.ok().getDescription()).isEmpty();
    assertThat(StatusData.error().getStatusCode()).isEqualTo(StatusCode.ERROR);
    assertThat(StatusData.error().getDescription()).isEmpty();
  }
}
