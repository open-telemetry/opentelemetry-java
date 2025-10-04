/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableFunctionData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableKeyValueAndUnitData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLinkData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLocationData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableMappingData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileDictionaryData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableStackData;
import java.util.Collections;

/**
 * This class allows for the assembly of the reference tables which form a ProfileDictionaryData.
 *
 * <p>It's effectively a builder, though without the fluent API. Instead, mutation methods return
 * the index of the offered element, this information being required to construct any element that
 * references into the tables.
 *
 * <p>Note this class does not enforce referential integrity, i.e. will accept an element which
 * references a non-existing element.
 *
 * <p>This class is not threadsafe and must be externally synchronized.
 */
public class ProfileDictionaryCompositor {

  // This implementation relies on the *Data interface impls having equals/hashCode that works.
  // The provided AutoValue_* ones do, but it's potentially fragile if used externally.

  private final DictionaryTable<MappingData> mappingTable = new DictionaryTable<>();
  private final DictionaryTable<LocationData> locationTable = new DictionaryTable<>();
  private final DictionaryTable<FunctionData> functionTable = new DictionaryTable<>();
  private final DictionaryTable<LinkData> linkTable = new DictionaryTable<>();
  private final DictionaryTable<String> stringTable = new DictionaryTable<>();
  private final DictionaryTable<KeyValueAndUnitData> attributeTable = new DictionaryTable<>();
  private final DictionaryTable<StackData> stackTable = new DictionaryTable<>();

  public ProfileDictionaryCompositor() {

    // The [0] element of each table should be a 'null' element, such that a 0 value pointer can be
    // used to indicate null / not set, in preference to requiring an 'optional' modifier on the
    // pointer declarations. The value of this placeholder element is one with all default field
    // values, such that it encodes to the minimal number of bytes.
    // These values are inlined here, as there is no other point of use.
    // They could be public static constants on this class or the corresponding Immutable*Data
    // classes if other use cases require them.

    mappingTable.putIfAbsent(ImmutableMappingData.create(0, 0, 0, 0, Collections.emptyList()));
    locationTable.putIfAbsent(
        ImmutableLocationData.create(0, 0, Collections.emptyList(), Collections.emptyList()));
    functionTable.putIfAbsent(ImmutableFunctionData.create(0, 0, 0, 0));
    linkTable.putIfAbsent(ImmutableLinkData.create("", ""));
    stringTable.putIfAbsent("");
    attributeTable.putIfAbsent(ImmutableKeyValueAndUnitData.create(0, Value.of(""), 0));
    stackTable.putIfAbsent(ImmutableStackData.create(Collections.emptyList()));
  }

  /**
   * Provides the contents of the dictionary tables as a ProfileDictionaryData.
   *
   * <p>The return value is a view, not a copy. No further elements should be inserted after this is
   * called as it would compromise the intended immutability of the result.
   *
   * @return a ProfileDictionaryData with the contents of the tables.
   */
  public ProfileDictionaryData getProfileDictionaryData() {
    return ImmutableProfileDictionaryData.create(
        mappingTable.getTable(),
        locationTable.getTable(),
        functionTable.getTable(),
        linkTable.getTable(),
        stringTable.getTable(),
        attributeTable.getTable(),
        stackTable.getTable());
  }

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * @param mappingData an element to store.
   * @return the index of the added or existing element.
   */
  public int putIfAbsent(MappingData mappingData) {
    return mappingTable.putIfAbsent(mappingData);
  }

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * @param locationData an element to store.
   * @return the index of the added or existing element.
   */
  public int putIfAbsent(LocationData locationData) {
    return locationTable.putIfAbsent(locationData);
  }

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * @param functionData an element to store.
   * @return the index of the added or existing element.
   */
  public int putIfAbsent(FunctionData functionData) {
    return functionTable.putIfAbsent(functionData);
  }

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * @param linkData an element to store.
   * @return the index of the added or existing element.
   */
  public int putIfAbsent(LinkData linkData) {
    return linkTable.putIfAbsent(linkData);
  }

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * @param string an element to store.
   * @return the index of the added or existing element.
   */
  public int putIfAbsent(String string) {
    return stringTable.putIfAbsent(string);
  }

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * @param keyValueAndUnitData an element to store.
   * @return the index of the added or existing element.
   */
  public int putIfAbsent(KeyValueAndUnitData keyValueAndUnitData) {
    return attributeTable.putIfAbsent(keyValueAndUnitData);
  }

  /**
   * Stores the provided element if an equivalent is not already present, and returns its index.
   *
   * @param stackData an element to store.
   * @return the index of the added or existing element.
   */
  public int putIfAbsent(StackData stackData) {
    return stackTable.putIfAbsent(stackData);
  }
}
