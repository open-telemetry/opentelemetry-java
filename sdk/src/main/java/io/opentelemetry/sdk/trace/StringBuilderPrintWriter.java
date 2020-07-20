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

package io.opentelemetry.sdk.trace;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Formatter;
import java.util.Locale;

/**
 * A {@link PrintWriter} that just delegates all methods to a {@link StringBuilder}, avoiding
 * synchronization.
 */
final class StringBuilderPrintWriter extends PrintWriter {

  // A Writer to pass to the PrintWriter superclass constructor. It is normally used as a lock but
  // we override all methods to avoid locking and don't need to pass a real Writer.
  private static final Writer NO_OP_WRITER =
      new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) {}

        @Override
        public void flush() {}

        @Override
        public void close() {}
      };

  private final StringBuilder sb;

  StringBuilderPrintWriter(StringBuilder sb) {
    super(NO_OP_WRITER);
    this.sb = sb;
  }

  @Override
  public void flush() {}

  @Override
  public void close() {}

  @Override
  public boolean checkError() {
    return false;
  }

  @Override
  protected void setError() {}

  @Override
  protected void clearError() {}

  @Override
  public void write(int c) {
    sb.append((char) c);
  }

  @Override
  public void write(char[] buf, int off, int len) {
    sb.append(buf, off, len);
  }

  @Override
  public void write(char[] buf) {
    sb.append(buf);
  }

  @Override
  public void write(String s, int off, int len) {
    sb.append(s, off, off + len);
  }

  @Override
  public void write(String s) {
    sb.append(s);
  }

  @Override
  public void print(boolean b) {
    sb.append(b);
  }

  @Override
  public void print(char c) {
    sb.append(c);
  }

  @Override
  public void print(int i) {
    sb.append(i);
  }

  @Override
  public void print(long l) {
    sb.append(l);
  }

  @Override
  public void print(float f) {
    sb.append(f);
  }

  @Override
  public void print(double d) {
    sb.append(d);
  }

  @Override
  public void print(char[] s) {
    sb.append(s);
  }

  @Override
  public void print(String s) {
    sb.append(s);
  }

  @Override
  public void print(Object obj) {
    sb.append(obj);
  }

  @Override
  public void println() {
    sb.append(System.lineSeparator());
  }

  @Override
  public void println(boolean x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(char x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(int x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(long x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(float x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(double x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(char[] x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(String x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public void println(Object x) {
    sb.append(x).append(System.lineSeparator());
  }

  @Override
  public PrintWriter printf(String format, Object... args) {
    return format(format, args);
  }

  @Override
  public PrintWriter printf(Locale l, String format, Object... args) {
    return format(l, format, args);
  }

  @Override
  public PrintWriter format(String format, Object... args) {
    // We implement format for good measure but don't expect it to be called, so reduce overhead in
    // the expected case by allocatting the Formatter here.
    new Formatter(sb).format(Locale.getDefault(), format, args);
    return this;
  }

  @Override
  public PrintWriter format(Locale l, String format, Object... args) {
    // We implement format for good measure but don't expect it to be called, so reduce overhead in
    // the expected case by allocatting the Formatter here.
    new Formatter(sb).format(l, format, args);
    return this;
  }

  @Override
  public PrintWriter append(CharSequence csq) {
    sb.append(csq);
    return this;
  }

  @Override
  public PrintWriter append(CharSequence csq, int start, int end) {
    sb.append(csq, start, end);
    return this;
  }

  @Override
  public PrintWriter append(char c) {
    sb.append(c);
    return this;
  }
}
