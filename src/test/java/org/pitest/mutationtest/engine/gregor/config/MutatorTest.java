/*
 * Copyright 2011 Henry Coles
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
package org.pitest.mutationtest.engine.gregor.config;

import org.junit.Test;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

public class MutatorTest {
    @Test
    public void providesRETURN_VALS() {
        assertProvides("RETURN_VALS");
    }

    private Collection<MethodMutatorFactory> parseStrings(final String... s) {
        return Mutator.fromStrings(asList(s));
    }

    private void assertProvides(String name) {
        assertThatCode(() -> parseStrings(name)).doesNotThrowAnyException();
    }
}
