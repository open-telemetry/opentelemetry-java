/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class StringEncoderWithoutUnsafeTest {

  // Simulate running in an environment without sun.misc.Unsafe e.g. when running a modular
  // application. To use sun.misc.Unsafe in modular application user would need to add dependency to
  // jdk.unsupported module or use --add-modules jdk.unsupported. Here we use a custom child first
  // class loader that does not delegate loading sun.misc classes to make sun.misc.Unsafe
  // unavailable.
  @Test
  void utf8EncodingWithoutUnsafe() throws Exception {
    ClassLoader testClassLoader =
        new ClassLoader(this.getClass().getClassLoader()) {
          @Override
          protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // don't allow loading sun.misc classes
            if (name.startsWith("sun.misc")) {
              throw new ClassNotFoundException(name);
            }
            // load io.opentelemetry in the custom loader
            if (name.startsWith("io.opentelemetry")) {
              synchronized (this) {
                Class<?> clazz = findLoadedClass(name);
                if (clazz != null) {
                  return clazz;
                }
                try (InputStream inputStream =
                    getParent().getResourceAsStream(name.replace(".", "/") + ".class")) {
                  if (inputStream != null) {
                    byte[] bytes = readBytes(inputStream);
                    // we don't bother to define packages or provide protection domain
                    return defineClass(name, bytes, 0, bytes.length);
                  }
                } catch (IOException exception) {
                  throw new ClassNotFoundException(name, exception);
                }
              }
            }
            return super.loadClass(name, resolve);
          }
        };

    // load test class in the custom loader and run the test
    Class<?> testClass = testClassLoader.loadClass(this.getClass().getName() + "$TestClass");
    assertThat(testClass.getClassLoader()).isEqualTo(testClassLoader);
    Runnable test = (Runnable) testClass.getConstructor().newInstance();
    test.run();
  }

  private static byte[] readBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];

    int readCount;
    while ((readCount = inputStream.read(buffer, 0, buffer.length)) != -1) {
      out.write(buffer, 0, readCount);
    }
    return out.toByteArray();
  }

  @SuppressWarnings("unused")
  public static class TestClass implements Runnable {

    @Override
    public void run() {
      // verify that unsafe can't be found
      assertThatThrownBy(() -> Class.forName("sun.misc.Unsafe"))
          .isInstanceOf(ClassNotFoundException.class);
      // test the methods that use unsafe
      assertThat(StringEncoder.getInstance().getUtf8Size("a")).isEqualTo(1);
      assertThat(testUtf8("a", 0)).isEqualTo("a");
    }

    static String testUtf8(String string, int utf8Length) {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        StringEncoder.getInstance().writeUtf8(codedOutputStream, string, utf8Length);
        codedOutputStream.flush();
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
      } catch (Exception exception) {
        throw new IllegalArgumentException(exception);
      }
    }
  }
}
