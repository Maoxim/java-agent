/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.classpath;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.util.Log;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class ClassPathByteArraySource implements ClassByteArraySource {

  private static final Logger LOG = Log.getLogger();

  private final ClassPath     classPath;

  public ClassPathByteArraySource() {
    this(new ClassPath());
  }

  public ClassPathByteArraySource(final ClassPath classPath) {
    this.classPath = classPath;
  }

  @Override
  public Optional<byte[]> getBytes(final String classname) {
    try {
      return Optional.ofNullable(this.classPath.getClassData(classname));
    } catch (final IOException e) {
      LOG.fine("Could not read class " + classname + ":" + e.getMessage());
      return Optional.empty();
    }
  }
}
