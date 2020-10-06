/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.trace.data.SpanData.Status;
import io.opentelemetry.trace.StatusCanonicalCode;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ImmutableStatus}. */
class ImmutableStatusTest {
  @Test
  void defaultConstants() {
    StatusCanonicalCode[] codes = StatusCanonicalCode.values();
    assertThat(codes).hasSize(3);
    assertThat(Status.unset().getCanonicalCode()).isEqualTo(StatusCanonicalCode.UNSET);
    assertThat(Status.unset().getDescription()).isNull();
    assertThat(Status.ok().getCanonicalCode()).isEqualTo(StatusCanonicalCode.OK);
    assertThat(Status.ok().getDescription()).isNull();
    assertThat(Status.error().getCanonicalCode()).isEqualTo(StatusCanonicalCode.ERROR);
    assertThat(Status.error().getDescription()).isNull();
  }

  @Test
  void generateCodeToStatus() {
    StatusCanonicalCode[] codes = StatusCanonicalCode.values();
    assertThat(ImmutableStatus.codeToStatus).hasSize(codes.length);
    for (StatusCanonicalCode code : codes) {
      assertThat(ImmutableStatus.codeToStatus.get(code)).isNotNull();
      assertThat(ImmutableStatus.codeToStatus.get(code).getCanonicalCode()).isEqualTo(code);
      assertThat(ImmutableStatus.codeToStatus.get(code).getDescription()).isNull();
    }
  }
}
