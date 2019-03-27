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
import javax.annotation.concurrent.Immutable;
import openconsensus.common.ExperimentalApi;
import openconsensus.internal.DefaultVisibilityForTesting;
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
@ExperimentalApi
public abstract class Resource {
  @DefaultVisibilityForTesting static final int MAX_LENGTH = 255;
  private static final String OC_RESOURCE_TYPE_ENV = "OC_RESOURCE_TYPE";
  private static final String OC_RESOURCE_LABELS_ENV = "OC_RESOURCE_LABELS";
  private static final String LABEL_LIST_SPLITTER = ",";
  private static final String LABEL_KEY_VALUE_SPLITTER = "=";
  private static final String ERROR_MESSAGE_INVALID_CHARS =
      " should be a ASCII string with a length greater than 0 and not exceed "
          + MAX_LENGTH
          + " characters.";
  private static final String ERROR_MESSAGE_INVALID_VALUE =
      " should be a ASCII string with a length not exceed " + MAX_LENGTH + " characters.";

  @javax.annotation.Nullable
  private static final String ENV_TYPE = parseResourceType(System.getenv(OC_RESOURCE_TYPE_ENV));

  private static final Map<String, String> ENV_LABEL_MAP =
      parseResourceLabels(System.getenv(OC_RESOURCE_LABELS_ENV));

  Resource() {}

  /**
   * Returns the type identifier for the resource.
   *
   * @return the type identifier for the resource.
   * @since 0.1.0
   */
  @javax.annotation.Nullable
  public abstract String getType();

  /**
   * Returns a map of labels that describe the resource.
   *
   * @return a map of labels.
   * @since 0.1.0
   */
  public abstract Map<String, String> getLabels();

  /**
   * Returns a {@link Resource}. This resource information is loaded from the OC_RESOURCE_TYPE and
   * OC_RESOURCE_LABELS environment variables.
   *
   * @return a {@code Resource}.
   * @since 0.1.0
   */
  public static Resource createFromEnvironmentVariables() {
    return createInternal(ENV_TYPE, ENV_LABEL_MAP);
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param type the type identifier for the resource.
   * @param labels a map of labels that describe the resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code labels} is null.
   * @throws IllegalArgumentException if type or label key or label value is not a valid printable
   *     ASCII string or exceed {@link #MAX_LENGTH} characters.
   * @since 0.1.0
   */
  public static Resource create(
      @javax.annotation.Nullable String type, Map<String, String> labels) {
    return createInternal(
        type,
        Collections.unmodifiableMap(
            new LinkedHashMap<String, String>(Utils.checkNotNull(labels, "labels"))));
  }

  /**
   * Returns a {@link Resource} that runs all input resources sequentially and merges their results.
   * In case a type of label key is already set, the first set value takes precedence.
   *
   * @param resources a list of resources.
   * @return a {@code Resource}.
   * @since 0.1.0
   */
  @javax.annotation.Nullable
  public static Resource mergeResources(List<Resource> resources) {
    Resource currentResource = null;
    for (Resource resource : resources) {
      currentResource = merge(currentResource, resource);
    }
    return currentResource;
  }

  private static Resource createInternal(
      @javax.annotation.Nullable String type, Map<String, String> labels) {
    return new AutoValue_Resource(type, labels);
  }

  /**
   * Creates a resource type from the OC_RESOURCE_TYPE environment variable.
   *
   * <p>OC_RESOURCE_TYPE: A string that describes the type of the resource prefixed by a domain
   * namespace, e.g. “kubernetes.io/container”.
   */
  @javax.annotation.Nullable
  static String parseResourceType(@javax.annotation.Nullable String rawEnvType) {
    if (rawEnvType != null && !rawEnvType.isEmpty()) {
      Utils.checkArgument(isValidAndNotEmpty(rawEnvType), "Type" + ERROR_MESSAGE_INVALID_CHARS);
      return rawEnvType.trim();
    }
    return rawEnvType;
  }

  /*
   * Creates a label map from the OC_RESOURCE_LABELS environment variable.
   *
   * <p>OC_RESOURCE_LABELS: A comma-separated list of labels describing the source in more detail,
   * e.g. “key1=val1,key2=val2”. Domain names and paths are accepted as label keys. Values may be
   * quoted or unquoted in general. If a value contains whitespaces, =, or " characters, it must
   * always be quoted.
   */
  static Map<String, String> parseResourceLabels(@javax.annotation.Nullable String rawEnvLabels) {
    if (rawEnvLabels == null) {
      return Collections.<String, String>emptyMap();
    } else {
      Map<String, String> labels = new HashMap<String, String>();
      String[] rawLabels = rawEnvLabels.split(LABEL_LIST_SPLITTER, -1);
      for (String rawLabel : rawLabels) {
        String[] keyValuePair = rawLabel.split(LABEL_KEY_VALUE_SPLITTER, -1);
        if (keyValuePair.length != 2) {
          continue;
        }
        String key = keyValuePair[0].trim();
        String value = keyValuePair[1].trim().replaceAll("^\"|\"$", "");
        Utils.checkArgument(isValidAndNotEmpty(key), "Label key" + ERROR_MESSAGE_INVALID_CHARS);
        Utils.checkArgument(isValid(value), "Label value" + ERROR_MESSAGE_INVALID_VALUE);
        labels.put(key, value);
      }
      return Collections.unmodifiableMap(labels);
    }
  }

  /**
   * Returns a new, merged {@link Resource} by merging two resources. In case of a collision, first
   * resource takes precedence.
   */
  @javax.annotation.Nullable
  private static Resource merge(
      @javax.annotation.Nullable Resource resource,
      @javax.annotation.Nullable Resource otherResource) {
    if (otherResource == null) {
      return resource;
    }
    if (resource == null) {
      return otherResource;
    }

    String mergedType = resource.getType() != null ? resource.getType() : otherResource.getType();
    Map<String, String> mergedLabelMap =
        new LinkedHashMap<String, String>(otherResource.getLabels());

    // Labels from resource overwrite labels from otherResource.
    for (Entry<String, String> entry : resource.getLabels().entrySet()) {
      mergedLabelMap.put(entry.getKey(), entry.getValue());
    }
    return createInternal(mergedType, Collections.unmodifiableMap(mergedLabelMap));
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
