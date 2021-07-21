/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.StatusCode;
import org.junit.jupiter.api.Test;

class ImmutableStatusDataTest {
  @Test
  void statuses() {
    StatusCode[] codes = StatusCode.values();
    for (StatusCode code : codes) {
      StatusData status = ImmutableStatusData.create(code, "");
      switch (code) {
        case UNSET:
          assertThat(status).isSameAs(StatusData.unset());
          break;
        case OK:
          assertThat(status).isSameAs(StatusData.ok());
          break;
        case ERROR:
          assertThat(status).isSameAs(StatusData.error());
          break;
      }
      assertThat(status).isNotNull();
      assertThat(status.getStatusCode()).isEqualTo(code);
      assertThat(status.getDescription()).isEmpty();
    }
  }
}
