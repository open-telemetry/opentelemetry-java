/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.propagation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

class CaseInsensitiveMap extends HashMap<String, String> {

  private static final long serialVersionUID = -4202518750189126871L;

  CaseInsensitiveMap(Map<String, String> carrier) {
    super(carrier);
  }

  @Override
  public String put(String key, String value) {
    return super.put(key.toLowerCase(Locale.ROOT), value);
  }

  @Override
  @Nullable
  public String get(Object key) {
    return super.get(((String) key).toLowerCase(Locale.ROOT));
  }
}
