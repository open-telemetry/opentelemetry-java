/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class StringMarshalState {

  @Param({"16", "512"})
  int stringSize;

  String asciiString;
  String latin1String;
  String unicodeString;

  @Setup
  public void setup() {
    asciiString = makeString('a', stringSize);
    latin1String = makeString('ä', stringSize);
    unicodeString = makeString('∆', stringSize);
  }

  private static String makeString(char c, int size) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size; i++) {
      sb.append(c);
    }
    return sb.toString();
  }
}
