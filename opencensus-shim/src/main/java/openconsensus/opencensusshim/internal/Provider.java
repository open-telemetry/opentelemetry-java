/*
 * Copyright 2019-17, OpenConsensus Authors
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

package openconsensus.opencensusshim.internal;

import java.util.ServiceConfigurationError;

/**
 * OpenConsensus service provider mechanism.
 *
 * <pre>{@code
 * // Initialize a variable using reflection.
 * foo = Provider.createInstance(
 *     Class.forName("FooImpl", true, classLoader), Foo.class);
 * }</pre>
 */
public final class Provider {
  private Provider() {}

  /**
   * Tries to create an instance of the given rawClass as a subclass of the given superclass.
   *
   * @param rawClass The class that is initialized.
   * @param superclass The initialized class must be a subclass of this.
   * @return an instance of the class given rawClass which is a subclass of the given superclass.
   * @throws ServiceConfigurationError if any error happens.
   */
  public static <T> T createInstance(Class<?> rawClass, Class<T> superclass) {
    try {
      return rawClass.asSubclass(superclass).getConstructor().newInstance();
    } catch (Exception e) {
      throw new ServiceConfigurationError(
          "Provider " + rawClass.getName() + " could not be instantiated.", e);
    }
  }
}
