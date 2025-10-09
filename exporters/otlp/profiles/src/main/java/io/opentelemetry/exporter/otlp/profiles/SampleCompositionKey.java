/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * A SampleCompositionKey represents the identity portion of an aggregation of observed data.
 * Observations (samples) having the same key can be merged to save space without loss of fidelity.
 */
@Immutable
public class SampleCompositionKey {

  // on the wire, a Sample's identity (i.e. 'primary key') is the tuple of
  // {stack_index, sorted(attribute_indices), link_index}
  private final int stackIndex;
  private final List<Integer> attributeIndices;
  private final int linkIndex;

  public SampleCompositionKey(int stackIndex, List<Integer> attributeIndices, int linkIndex) {
    this.stackIndex = stackIndex;
    List<Integer> tmp = new ArrayList<>(attributeIndices);
    Collections.sort(tmp);
    this.attributeIndices = Collections.unmodifiableList(tmp);
    this.linkIndex = linkIndex;
  }

  public int getStackIndex() {
    return stackIndex;
  }

  public List<Integer> getAttributeIndices() {
    return attributeIndices;
  }

  public int getLinkIndex() {
    return linkIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SampleCompositionKey)) {
      return false;
    }
    SampleCompositionKey that = (SampleCompositionKey) o;
    return stackIndex == that.stackIndex
        && linkIndex == that.linkIndex
        && Objects.equals(attributeIndices, that.attributeIndices);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stackIndex, attributeIndices, linkIndex);
  }
}
