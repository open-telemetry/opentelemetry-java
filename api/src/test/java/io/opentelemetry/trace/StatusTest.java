/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Status}. */
class StatusTest {
  @Test
  void status_Ok() {
    assertThat(Status.OK.getCanonicalCode()).isEqualTo(Status.CanonicalCode.OK);
    assertThat(Status.OK.getDescription()).isNull();
    assertThat(Status.OK.isOk()).isTrue();
  }

  @Test
  void createStatus_WithDescription() {
    Status status = Status.UNKNOWN.withDescription("This is an error.");
    assertThat(status.getCanonicalCode()).isEqualTo(Status.CanonicalCode.UNKNOWN);
    assertThat(status.getDescription()).isEqualTo("This is an error.");
    assertThat(status.isOk()).isFalse();
  }

  @Test
  void status_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(Status.OK, Status.OK.withDescription(null));
    tester.addEqualityGroup(
        Status.CANCELLED.withDescription("ThisIsAnError"),
        Status.CANCELLED.withDescription("ThisIsAnError"));
    tester.addEqualityGroup(Status.UNKNOWN.withDescription("This is an error."));
    tester.testEquals();
  }
}
