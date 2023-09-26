/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class AttributesGenerator {

  private AttributesGenerator() {}

  /**
   * Generates a list of unique attributes, with a single attribute key, and random value.
   *
   * @param uniqueAttributesCount The amount of unique attribute sets to generate
   * @return The list of generates {@link Attributes}
   */
  public static List<Attributes> generate(int uniqueAttributesCount) {
    Random random = new Random();
    HashSet<String> attributeSet = new HashSet<>();
    ArrayList<Attributes> attributesList = new ArrayList<>(uniqueAttributesCount);
    String last = "aaaaaaaaaaaaaaaaaaaaaaaaaa";
    for (int i = 0; i < uniqueAttributesCount; i++) {
      char[] chars = last.toCharArray();
      int attempts = 0;
      do {
        chars[random.nextInt(last.length())] = (char) (random.nextInt(26) + 'a');
      } while (attributeSet.contains(new String(chars)) && ++attempts < 1000);
      if (attributeSet.contains(new String(chars))) {
        throw new IllegalStateException("Couldn't create new random attributes");
      }
      last = new String(chars);
      attributesList.add(Attributes.builder().put("key", last).build());
      attributeSet.add(last);
    }

    return attributesList;
  }
}
