package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import java.util.List;

public class ExponentialHistogramBucketsAssert extends
    AbstractAssert<ExponentialHistogramBucketsAssert, ExponentialHistogramBuckets> {

  protected ExponentialHistogramBucketsAssert(ExponentialHistogramBuckets actual) {
    super(actual, ExponentialHistogramBucketsAssert.class);
  }

  public ExponentialHistogramBucketsAssert hasCounts(List<Long> expected) {
    isNotNull();
    Assertions.assertThat(actual.getBucketCounts()).as("bucketCounts").isEqualTo(expected);
    return this;
  }

  public ExponentialHistogramBucketsAssert hasTotalCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getTotalCount()).as("totalCount").isEqualTo(expected);
    return this;
  }

  public ExponentialHistogramBucketsAssert hasOffset(int expected) {
    isNotNull();
    Assertions.assertThat(actual.getOffset()).as("offset").isEqualTo(expected);
    return this;
  }
}
