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
package org.pitest.mutationtest.engine.gregor.config;

import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;
import org.pitest.util.ServiceLoader;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Mutator {
  private static final Logger log = Log.getLogger();
  private static final Map<String, List<MethodMutatorFactory>> MUTATORS;

  static {
    Collection<MutatorGroup> mutatorFactories = ServiceLoader.load(MutatorGroup.class, IsolationUtils.getContextClassLoader());

    Collection<MethodMutatorFactory> ms = ServiceLoader.load(MethodMutatorFactory.class, IsolationUtils.getContextClassLoader());

    MUTATORS = ms.stream()
            .collect(Collectors.groupingBy(MethodMutatorFactory::getName));

    mutatorFactories.stream()
            .forEach(m -> m.register(MUTATORS));
  }

  public static Collection<MethodMutatorFactory> all() {
    return fromStrings(allMutatorIds());
  }

  public static Collection<String> allMutatorIds() {
    return MUTATORS.keySet();
  }

  private static Collection<MethodMutatorFactory> combine(
      Collection<MethodMutatorFactory> a, Collection<MethodMutatorFactory> b) {
    final List<MethodMutatorFactory> l = new ArrayList<>(a);
    l.addAll(b);
    return l;
  }

  /**
   * Proposed new defaults - replaced the RETURN_VALS mutator with the new more stable set
   */
  public static Collection<MethodMutatorFactory> newDefaults() {
    return combine(group(ReturnValsMutator.RETURN_VALS), betterReturns());
  }


  private static Collection<MethodMutatorFactory> betterReturns() {
    //todo add more mutators
    return group(null);
  }

  private static Collection<MethodMutatorFactory> group(
      final MethodMutatorFactory... ms) {
    return Arrays.asList(ms);
  }

  public static Collection<MethodMutatorFactory> byName(final String name) {
    return FCollection.map(MUTATORS.get(name),
        Prelude.id(MethodMutatorFactory.class));
  }

  public static Collection<MethodMutatorFactory> fromStrings(
      final Collection<String> names) {

    List<String> exclusions = names.stream()
            .filter(s -> s.startsWith("-"))
            .map(s -> s.substring(1))
            .collect(Collectors.toList());

    List<String> inclusions = names.stream()
            .filter(s -> !s.startsWith("-"))
            .collect(Collectors.toList());

    final Set<MethodMutatorFactory> unique = new TreeSet<>(compareId());
    log.info("loaded " + MUTATORS.size() + " mutator(s)");
    FCollection.flatMapTo(inclusions, fromString(MUTATORS), unique);

    final Set<MethodMutatorFactory> excluded = new TreeSet<>(compareId());
    FCollection.flatMapTo(exclusions, fromString(MUTATORS), excluded);

    unique.removeAll(excluded);

    return unique;
  }

  private static Comparator<? super MethodMutatorFactory> compareId() {
    return Comparator.comparing(MethodMutatorFactory::getGloballyUniqueId);
  }

  private static Function<String, Iterable<MethodMutatorFactory>> fromString(Map<String, List<MethodMutatorFactory>> mutators) {
    return a -> {

      if (a.equals("ALL")) {
        return all();
      }

      final Iterable<MethodMutatorFactory> i = mutators.get(a);
      if (i == null) {
        throw new Error(a);
      }
      return i;
    };
  }

}
