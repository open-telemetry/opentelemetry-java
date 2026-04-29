/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.jfr;

import io.opentelemetry.sdk.profiles.ProfilesDictionaryCompositor;
import io.opentelemetry.sdk.profiles.SampleCompositionBuilder;
import io.opentelemetry.sdk.profiles.SampleCompositionKey;
import io.opentelemetry.sdk.profiles.data.SampleData;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jdk.jfr.consumer.RecordedEvent;

/**
 * Converter for batching a steam of recorded jfr.ExecutionSample events into a format suitable for
 * consumption in a ProfileData i.e. for OTLP export. Similar converters, or a more generalized
 * converter, are need for each JFR event type.
 */
public class JfrExecutionSampleEventConverter {

  private JfrExecutionSampleEventConverter() {}

  /**
   * Returns a new converter, initialized with an empty dictionary.
   *
   * @return a new JfrExecutionSampleEventConverter.
   */
  public static JfrExecutionSampleEventConverter create() {
    return new JfrExecutionSampleEventConverter();
  }

  /*
   * The profiles signal encoding uses dictionary lookup tables to save space by deduplicating
   * repeated object occurrences. The dictionary compositor is used to assemble these tables.
   */
  private final ProfilesDictionaryCompositor profilesDictionaryCompositor =
      new ProfilesDictionaryCompositor();

  /*
   * stack frames are dictionary encoded in multiple steps.
   * first, frames are converted to Locations, each of which is placed in the dictionary.
   * Then the stack as a whole is represented as an array of those Locations,
   * and the Stack message itself is also placed in the dictionary.
   * This assembly is handled by a JfrLocationDataCompositor wrapping the dictionary
   */
  private final JfrLocationDataCompositor locationCompositor =
      JfrLocationDataCompositor.create(profilesDictionaryCompositor);

  /*
   * Samples are occurrences of the same observation, with an optional value and timestamp.
   * In JFR, for each given event type, a SampleCompositionBuilder is used to split the
   * events (observations) by key (stack+metadata) and record the timestamps.
   * If processing multiple event types, a Map<EventType,SampleCompositionBuilder> would be used.
   */
  private final SampleCompositionBuilder sampleCompositionBuilder = new SampleCompositionBuilder();

  /**
   * Convert and add a JFR event, if of appropriate type.
   *
   * @param recordedEvent the event to process.
   */
  public void accept(RecordedEvent recordedEvent) {
    if (!"jdk.ExecutionSample".equals(recordedEvent.getEventType().getName())) {
      return;
    }

    int stackIndex = locationCompositor.putIfAbsent(recordedEvent.getStackTrace().getFrames());
    SampleCompositionKey key = new SampleCompositionKey(stackIndex, Collections.emptyList(), 0);
    Instant instant = recordedEvent.getStartTime();
    long epochNanos = TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    sampleCompositionBuilder.add(key, null, epochNanos);
  }

  /**
   * Gets the underlying dictionary storage.
   *
   * @return the ProfilesDictionaryCompositor used by this converter.
   */
  public ProfilesDictionaryCompositor getProfilesDictionaryCompositor() {
    return profilesDictionaryCompositor;
  }

  /**
   * Gets the samples assembled from the accepted events.
   *
   * @return the data samples.
   */
  public List<SampleData> getSamples() {
    return sampleCompositionBuilder.build();
  }
}
