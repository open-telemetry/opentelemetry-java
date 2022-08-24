/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class ViewConfigSpecification {

  static AutoValue_ViewConfigSpecification.Builder builder() {
    return new AutoValue_ViewConfigSpecification.Builder();
  }

  abstract SelectorSpecification getSelectorSpecification();

  abstract ViewSpecification getViewSpecification();

  @AutoValue.Builder
  interface Builder {
    Builder selectorSpecification(SelectorSpecification selectorSpecification);

    Builder viewSpecification(ViewSpecification viewSpecification);

    ViewConfigSpecification build();
  }
}
