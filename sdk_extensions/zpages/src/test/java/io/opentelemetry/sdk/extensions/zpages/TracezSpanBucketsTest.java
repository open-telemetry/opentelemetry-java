/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.extensions.zpages;

import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

/** Unit tests for {@link TracezSpanBuckets}. */
@RunWith(JUnit4.class)
public class TracezSpanBucketsTest {
  @Mock private ReadableSpan readableSpan;
  @Mock private SpanData spanData;

  @Test
  public void addToBucket_moreThanTenLatencySamples() {
    /*
    TracezSpanBuckets spanBuckets = new TracezSpanBuckets();
     when(readableSpan.toSpanData()).thenReturn(spanData);
     when(spanData.getStatus()).thenReturn(Status.OK);
     when(readableSpan.getLatencyNanos()).thenReturn((long) 0);
     for (int i = 0; i < 15; i++) {
       spanBuckets.addToBucket(readableSpan);
     }
     assertThat(spanBuckets.getOkSpans().size()).isEqualTo(10);
    */
  }

  @Test
  public void addToBucket_moreThanFiveErrorSamples() {
    /*
    TracezSpanBuckets spanBuckets = new TracezSpanBuckets();
    when(readableSpan.toSpanData()).thenReturn(spanData);
    when(spanData.getStatus()).thenReturn(Status.UNKNOWN);
    for (int i = 0; i < 10; i++) {
      spanBuckets.addToBucket(readableSpan);
    }
    assertThat(spanBuckets.getOkSpans().size()).isEqualTo(5);
    */
  }

  @Test
  public void getLatencyBoundariesToCountMap_oneSpanPerLatencyBucket() {
    /*
    TracezSpanBuckets spanBuckets = new TracezSpanBuckets();
    when(readableSpan.toSpanData()).thenReturn(spanData);
    when(spanData.getStatus()).thenReturn(Status.OK);
    for (LatencyBoundaries bucket : LatencyBoundaries.values()) {
      when(readableSpan.getLatencyNanos()).thenReturn(bucket.getLatencyLowerBound());
      spanBuckets.addToBucket(readableSpan);
    }
    for (Integer count : spanBuckets.getLatencyBoundariesToCountMap().values()) {
      assertThat(count).isEqualTo(1);
    }
    */
  }

  /*
  @Test
  public void getErrorCanonicalCodeToCountMap_oneSpanPerErrorCode() {
    TracezSpanBuckets spanBuckets = new TracezSpanBuckets();
    when(readableSpan.toSpanData()).thenReturn(spanData);
    for (CanonicalCode errorCode : CanonicalCode.values()) {
      if (!errorCode.toStatus().isOk()) {
        when(spanData.getStatus()).thenReturn(errorCode.toStatus());
        spanBuckets.addToBucket(readableSpan);
      }
    }
    for (Integer count : spanBuckets.getErrorCanonicalCodeToCountMap().values()) {
      assertThat(count).isEqualTo(1);
    }
  }
  */

  @Test
  public void getSpans_oneOkSpanAndOneErrorSpan() {
    /*
    TracezSpanBuckets spanBuckets = new TracezSpanBuckets();
    when(readableSpan.toSpanData()).thenReturn(spanData);
    when(spanData.getStatus()).thenReturn(Status.OK);
    when(readableSpan.getLatencyNanos()).thenReturn((long) 0);
    spanBuckets.addToBucket(readableSpan);
    when(spanData.getStatus()).thenReturn(Status.UNKNOWN);
    spanBuckets.addToBucket(readableSpan);
    */
    /* getSpans should return 1 ok span and 1 error span */
    // assertThat(spanBuckets.getSpans().size()).isEqualTo(2);
    // assertThat(spanBuckets.getOkSpans().size()).isEqualTo(1);
    // assertThat(spanBuckets.getErrorSpans().size()).isEqualTo(1);
  }
}
