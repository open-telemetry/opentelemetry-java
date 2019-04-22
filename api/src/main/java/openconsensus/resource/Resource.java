/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.resource;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.StringUtils;
import openconsensus.internal.Utils;

/**
 * {@link Resource} represents a resource, which capture identifying information about the entities
 * for which signals (stats or traces) are reported. It further provides a framework for detection
 * of resource information from the environment and progressive population as signals propagate from
 * the core instrumentation library to a backend's exporter.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Resource {
  private static final int MAX_LENGTH = 255;
  private static final String OC_RESOURCE_LABELS_ENV = "OC_RESOURCE_LABELS";
  private static final String LABEL_LIST_SPLITTER = ",";
  private static final String LABEL_KEY_VALUE_SPLITTER = "=";
  private static final String ERROR_MESSAGE_INVALID_CHARS =
      " should be a ASCII string with a length greater than 0 and not exceed "
          + MAX_LENGTH
          + " characters.";
  private static final String ERROR_MESSAGE_INVALID_VALUE =
      " should be a ASCII string with a length not exceed " + MAX_LENGTH + " characters.";

  private static final Map<String, String> ENV_LABEL_MAP =
      parseResourceLabels(System.getenv(OC_RESOURCE_LABELS_ENV));

  private static final Resource empty =
      new AutoValue_Resource(Collections.<String, String>emptyMap());

  Resource() {}

  public static Resource getEmpty() {
    return empty;
  }

  /**
   * Returns a map of labels that describe the resource.
   *
   * @return a map of labels.
   * @since 0.1.0
   */
  public abstract Map<String, String> getLabels();

  /**
   * Returns a {@link Resource}. This resource information is loaded from the OC_RESOURCE_LABELS
   * environment variable.
   *
   * @return a {@code Resource}.
   * @since 0.1.0
   */
  public static Resource createFromEnvironmentVariable() {
    return new AutoValue_Resource(ENV_LABEL_MAP);
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param labels a map of labels that describe the resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code labels} is null.
   * @throws IllegalArgumentException if label key or label value is not a valid printable ASCII
   *     string or exceed {@link #MAX_LENGTH} characters.
   * @since 0.1.0
   */
  public static Resource create(Map<String, String> labels) {
    checkLabels(Utils.checkNotNull(labels, "labels"));
    return new AutoValue_Resource(Collections.unmodifiableMap(new LinkedHashMap<>(labels)));
  }

  /**
   * Returns a {@link Resource} that runs all input resources sequentially and merges their results.
   * In case a label key is already set, the first set value takes precedence.
   *
   * @param resources a list of resources.
   * @return a {@code Resource}.
   * @since 0.1.0
   */
  @Nullable
  public static Resource mergeResources(List<Resource> resources) {
    Resource currentResource = null;
    for (Resource resource : resources) {
      currentResource = merge(currentResource, resource);
    }
    return currentResource;
  }

  /*
   * Creates a label map from the OC_RESOURCE_LABELS environment variable.
   *
   * <p>OC_RESOURCE_LABELS: A comma-separated list of labels describing the source in more detail,
   * e.g. “key1=val1,key2=val2”. Domain names and paths are accepted as label keys. Values may be
   * quoted or unquoted in general. If a value contains whitespaces, =, or " characters, it must
   * always be quoted.
   */
  private static Map<String, String> parseResourceLabels(@Nullable String rawEnvLabels) {
    if (rawEnvLabels == null) {
      return Collections.emptyMap();
    } else {
      Map<String, String> labels = new HashMap<>();
      String[] rawLabels = rawEnvLabels.split(LABEL_LIST_SPLITTER, -1);
      for (String rawLabel : rawLabels) {
        String[] keyValuePair = rawLabel.split(LABEL_KEY_VALUE_SPLITTER, -1);
        if (keyValuePair.length != 2) {
          continue;
        }
        String key = keyValuePair[0].trim();
        String value = keyValuePair[1].trim().replaceAll("^\"|\"$", "");
        labels.put(key, value);
      }
      checkLabels(labels);
      return Collections.unmodifiableMap(labels);
    }
  }

  /**
   * Returns a new, merged {@link Resource} by merging two resources. In case of a collision, first
   * resource takes precedence.
   */
  @Nullable
  private static Resource merge(@Nullable Resource resource, @Nullable Resource otherResource) {
    if (otherResource == null) {
      return resource;
    }
    if (resource == null) {
      return otherResource;
    }

    Map<String, String> mergedLabelMap = new LinkedHashMap<>(otherResource.getLabels());
    // Labels from resource overwrite labels from otherResource.
    for (Entry<String, String> entry : resource.getLabels().entrySet()) {
      mergedLabelMap.put(entry.getKey(), entry.getValue());
    }
    return new AutoValue_Resource(Collections.unmodifiableMap(mergedLabelMap));
  }

  private static void checkLabels(Map<String, String> labels) {
    for (Entry<String, String> entry : labels.entrySet()) {
      Utils.checkArgument(
          isValidAndNotEmpty(entry.getKey()), "Label key" + ERROR_MESSAGE_INVALID_CHARS);
      Utils.checkArgument(isValid(entry.getValue()), "Label value" + ERROR_MESSAGE_INVALID_VALUE);
    }
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length not
   * exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValid(String name) {
    return name.length() <= MAX_LENGTH && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length
   * greater than 0 and not exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValidAndNotEmpty(String name) {
    return !name.isEmpty() && isValid(name);
  }
}
