/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimitsModel;
import io.opentelemetry.sdk.trace.SpanLimits;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SpanLimitsFactoryTest {

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(SpanLimitsAndAttributeLimits model, SpanLimits expectedSpanLimits) {
    assertThat(SpanLimitsFactory.getInstance().create(model, mock(DeclarativeConfigContext.class)))
        .isEqualTo(expectedSpanLimits);
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(SpanLimitsAndAttributeLimits.create(null, null), SpanLimits.getDefault()),
        Arguments.of(
            SpanLimitsAndAttributeLimits.create(new AttributeLimitsModel(), new SpanLimitsModel()),
            SpanLimits.getDefault()),
        Arguments.of(
            SpanLimitsAndAttributeLimits.create(
                new AttributeLimitsModel()
                    .withAttributeCountLimit(1)
                    .withAttributeValueLengthLimit(2),
                new SpanLimitsModel()),
            SpanLimits.builder().setMaxNumberOfAttributes(1).setMaxAttributeValueLength(2).build()),
        Arguments.of(
            SpanLimitsAndAttributeLimits.create(
                new AttributeLimitsModel()
                    .withAttributeCountLimit(1)
                    .withAttributeValueLengthLimit(2),
                new SpanLimitsModel()
                    .withAttributeCountLimit(3)
                    .withAttributeValueLengthLimit(4)
                    .withEventCountLimit(5)
                    .withLinkCountLimit(6)
                    .withEventAttributeCountLimit(7)
                    .withLinkAttributeCountLimit(8)),
            SpanLimits.builder()
                .setMaxNumberOfAttributes(3)
                .setMaxAttributeValueLength(4)
                .setMaxNumberOfEvents(5)
                .setMaxNumberOfLinks(6)
                .setMaxNumberOfAttributesPerEvent(7)
                .setMaxNumberOfAttributesPerLink(8)
                .build()));
  }
}
