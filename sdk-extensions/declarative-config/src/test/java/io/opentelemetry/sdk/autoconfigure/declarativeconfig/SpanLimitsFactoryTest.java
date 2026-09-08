/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel;
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
        Arguments.argumentSet(
            "null limits",
            SpanLimitsAndAttributeLimits.create(null, null),
            SpanLimits.getDefault()),
        Arguments.argumentSet(
            "empty models",
            SpanLimitsAndAttributeLimits.create(new AttributeLimitsModel(), new SpanLimitsModel()),
            SpanLimits.getDefault()),
        Arguments.argumentSet(
            "attribute limits only",
            SpanLimitsAndAttributeLimits.create(
                new AttributeLimitsModel()
                    .setAttributeCountLimit(1)
                    .setAttributeValueLengthLimit(2),
                new SpanLimitsModel()),
            SpanLimits.builder().setMaxNumberOfAttributes(1).setMaxAttributeValueLength(2).build()),
        Arguments.argumentSet(
            "span limits override attribute limits",
            SpanLimitsAndAttributeLimits.create(
                new AttributeLimitsModel()
                    .setAttributeCountLimit(1)
                    .setAttributeValueLengthLimit(2),
                new SpanLimitsModel()
                    .setAttributeCountLimit(3)
                    .setAttributeValueLengthLimit(4)
                    .setEventCountLimit(5)
                    .setLinkCountLimit(6)
                    .setEventAttributeCountLimit(7)
                    .setLinkAttributeCountLimit(8)),
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
