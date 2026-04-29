/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.jfr;

import io.opentelemetry.sdk.profiles.ProfilesDictionaryCompositor;
import io.opentelemetry.sdk.profiles.data.FunctionData;
import io.opentelemetry.sdk.profiles.data.LineData;
import io.opentelemetry.sdk.profiles.data.LocationData;
import io.opentelemetry.sdk.profiles.data.StackData;
import java.util.Collections;
import java.util.List;
import jdk.jfr.consumer.RecordedFrame;

/**
 * Allows for the conversion and storage of JFR thread stacks in the dictionary encoding structure
 * used by OTLP profile signal exporters.
 *
 * <p>The compositor resembles a builder, though without the fluent API. Instead, mutation methods
 * return the index of the offered element, this information being required to construct any element
 * that references into the tables.
 *
 * <p>This class is not threadsafe and must be externally synchronized.
 */
public class JfrLocationDataCompositor {

  private final ProfilesDictionaryCompositor profilesDictionaryCompositor;

  /**
   * Returns a new JFR Compositor, using the offered Dictionary for storage.
   *
   * @param profilesDictionaryCompositor the underlying dictionary state store.
   * @return a new ProfilesDictionaryCompositor.
   */
  public static JfrLocationDataCompositor create(
      ProfilesDictionaryCompositor profilesDictionaryCompositor) {
    return new JfrLocationDataCompositor(profilesDictionaryCompositor);
  }

  /**
   * Wrap the given dictionary with additional JFR-specific stack data handling functionality.
   *
   * @param profilesDictionaryCompositor the underlying storage.
   */
  private JfrLocationDataCompositor(ProfilesDictionaryCompositor profilesDictionaryCompositor) {
    this.profilesDictionaryCompositor = profilesDictionaryCompositor;
  }

  /**
   * Stores the provided list of frames as a StackData element in the dictionary if an equivalent is
   * not already present, and returns its index.
   *
   * @param frameList the JFR stack data.
   * @return the index of the added or existing StackData element.
   */
  public int putIfAbsent(List<RecordedFrame> frameList) {

    List<Integer> locationIndices = frameList.stream().map(this::frameToLocation).toList();

    StackData stackData = StackData.create(locationIndices);
    int stackIndex = profilesDictionaryCompositor.putIfAbsent(stackData);
    return stackIndex;
  }

  /**
   * Convert a single frame of a stack to a LocationData, store it and its components in the
   * dictionary and return its index.
   *
   * @param frame the source data
   * @return the LocationData storage index in the dictionary
   */
  protected int frameToLocation(RecordedFrame frame) {

    // the LocationData references several components which need creating and placing in their
    // respective dictionary tables

    String name = nameFrom(frame);
    int nameStringIndex = profilesDictionaryCompositor.putIfAbsent(name);

    FunctionData functionData = FunctionData.create(nameStringIndex, 0, 0, 0);
    int functionIndex = profilesDictionaryCompositor.putIfAbsent(functionData);

    int lineNumber = frame.getLineNumber() != -1 ? frame.getLineNumber() : 0;
    LineData lineData = LineData.create(functionIndex, lineNumber, 0);

    LocationData locationData =
        LocationData.create(0, 0, List.of(lineData), Collections.emptyList());

    int locationIndex = profilesDictionaryCompositor.putIfAbsent(locationData);
    return locationIndex;
  }

  /**
   * Construct a name String from the frame. Note that the wire spec and semantic conventions don't
   * define a specific string format. Override this method to customize the conversion.
   *
   * @param frame the JFR frame data.
   * @return the name as a String.
   */
  protected String nameFrom(RecordedFrame frame) {
    String name = frame.getMethod().getType() != null ? frame.getMethod().getType().getName() : "";
    name += ".";
    name += frame.getMethod().getName() != null ? frame.getMethod().getName() : "";
    return name;
  }
}
