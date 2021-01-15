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
    assertThat(StatusData.unset().getDescription()).isNull();
    assertThat(StatusData.ok().getStatusCode()).isEqualTo(StatusCode.OK);
    assertThat(StatusData.ok().getDescription()).isNull();
    assertThat(StatusData.error().getStatusCode()).isEqualTo(StatusCode.ERROR);
    assertThat(StatusData.error().getDescription()).isNull();
  }

  @Test
  void generateCodeToStatus() {
    StatusCode[] codes = StatusCode.values();
    assertThat(ImmutableStatusData.codeToStatus).hasSize(codes.length);
    for (StatusCode code : codes) {
      assertThat(ImmutableStatusData.codeToStatus.get(code)).isNotNull();
      assertThat(ImmutableStatusData.codeToStatus.get(code).getStatusCode()).isEqualTo(code);
      assertThat(ImmutableStatusData.codeToStatus.get(code).getDescription()).isNull();
    }
  }
}
