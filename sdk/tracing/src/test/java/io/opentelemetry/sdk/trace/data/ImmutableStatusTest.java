/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.trace.StatusCanonicalCode;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ImmutableStatus}. */
class ImmutableStatusTest {
  @Test
  void defaultConstants() {
    StatusCanonicalCode[] codes = StatusCanonicalCode.values();
    assertThat(codes).hasSize(3);
    assertThat(ImmutableStatus.UNSET.getCanonicalCode()).isEqualTo(StatusCanonicalCode.UNSET);
    assertThat(ImmutableStatus.UNSET.getDescription()).isNull();
    assertThat(ImmutableStatus.OK.getCanonicalCode()).isEqualTo(StatusCanonicalCode.OK);
    assertThat(ImmutableStatus.OK.getDescription()).isNull();
    assertThat(ImmutableStatus.ERROR.getCanonicalCode()).isEqualTo(StatusCanonicalCode.ERROR);
    assertThat(ImmutableStatus.ERROR.getDescription()).isNull();
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
