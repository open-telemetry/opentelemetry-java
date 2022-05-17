/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import org.junit.jupiter.api.Test;

class RegisteredViewTest {

  @Test
  void stringRepresentation() {
    assertThat(
            RegisteredView.create(
                    InstrumentSelector.builder()
                        .setName("name")
                        .setType(InstrumentType.COUNTER)
                        .setMeterName("meter-name")
                        .setMeterVersion("meter-version")
                        .setMeterSchemaUrl("meter-schema-url")
                        .build(),
                    View.builder()
                        .setName("name")
                        .setDescription("description")
                        .setAggregation(Aggregation.sum())
                        .build(),
                    AttributesProcessor.noop(),
                    SourceInfo.fromCurrentStack())
                .toString())
        .isEqualTo(
            "RegisteredView{"
                + "instrumentSelector=InstrumentSelector{instrumentType=COUNTER, instrumentName=name, meterName=meter-name, meterVersion=meter-version, meterSchemaUrl=meter-schema-url}, "
                + "view=View{name=name, description=description, aggregation=SumAggregation, attributesProcessor=NoopAttributesProcessor{}}"
                + "}");
  }
}
