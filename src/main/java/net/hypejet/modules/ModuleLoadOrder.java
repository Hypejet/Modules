/*
 * Copyright 2026 Hypejet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hypejet.modules;

import net.hypejet.modules.exception.CircularModuleDependenciesException;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.StringJoiner;

@NullMarked
final class ModuleLoadOrder<E> {

    private static final String CIRCULAR_DEPENDENCIES_ERROR_SEPARATOR = "--------------------------------------------------";
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleLoadOrder.class);

    private final Map<Class<? extends Module<? super E>>, ModuleEntry<E, ?>> unorderedEntries;
    private final Map<Class<?>, Class<? extends Module<? super E>>> superclassModules;

    private final SequencedMap<Class<? extends Module<? super E>>, ModuleEntry<E, ?>> orderedEntries = new LinkedHashMap<>();

    ModuleLoadOrder(Map<Class<? extends Module<? super E>>, ModuleEntry<E, ?>> unorderedEntries,
                    Map<Class<?>, Class<? extends Module<? super E>>> superclassModules) {
        this.unorderedEntries = new HashMap<>(unorderedEntries);
        this.superclassModules = Map.copyOf(superclassModules);
    }

    void process() {
        /* Do not use the built-in loops to prevent concurrent modification
           exceptions from the processModule method removing associations. */
        while (!this.unorderedEntries.isEmpty())
            this.processModule(this.unorderedEntries.values().iterator().next(), List.of());
    }

    List<ModuleEntry<E, ?>> getResult() {
        return new ArrayList<>(this.orderedEntries.values());
    }

    private <M extends Module<? super E>> void processModule(ModuleEntry<E, M> entry, List<Class<?>> dependants) {
        Class<M> clazz = entry.clazz();

        try {
            if (this.orderedEntries.containsKey(clazz)) return;
            if (!this.processDependencies(entry, dependants)) return;
            this.orderedEntries.put(clazz, entry);
        } catch (CircularModuleDependenciesException exception) {
            throw exception; // Re-throw the exception, we will not process the modules anymore
        } catch (Throwable throwable) {
            LOGGER.error("An error occurred while processing a module", throwable);
        } finally {
            // Remove the entry here to prevent re-processing the same module if it failed to process as a dependency
            this.unorderedEntries.remove(clazz);
        }
    }

    private <M extends Module<? super E>> boolean processDependencies(ModuleEntry<E, M> module, List<Class<?>> dependants) {
        Class<M> clazz = module.clazz();

        Set<Class<?>> dependencies = new HashSet<>(module.requiredDependencies());
        dependencies.addAll(module.optionalDependencies());

        for (Class<?> dependencyClass : dependencies) {
            Class<?> specifiedDependencyClass = dependencyClass;

            if (this.orderedEntries.containsKey(dependencyClass)) continue;
            boolean required = module.requiredDependencies().contains(dependencyClass);

            if (!this.unorderedEntries.containsKey(dependencyClass)) {
                dependencyClass = this.superclassModules.get(dependencyClass);

                if (dependencyClass == null) {
                    if (!required) continue;
                    logDependencyFailed(clazz, specifiedDependencyClass);
                    return false;
                }

                if (this.orderedEntries.containsKey(dependencyClass)) continue;
            }

            ModuleEntry<E, ?> dependencyModule = this.unorderedEntries.get(dependencyClass);
            if (dependencyModule == null) {
                if (!required) continue;
                logDependencyFailed(clazz, specifiedDependencyClass);
                return false;
            }

            if (dependants.contains(dependencyClass) || clazz == dependencyClass) {
                LOGGER.error(CIRCULAR_DEPENDENCIES_ERROR_SEPARATOR);
                LOGGER.error("Circular module dependencies detected:");

                StringJoiner joiner = new StringJoiner(" -> ");
                dependants.forEach(dependant -> joiner.add(dependant.getSimpleName()));
                joiner.add(clazz.getSimpleName());
                joiner.add(dependencyClass.getSimpleName());

                LOGGER.error(joiner.toString());
                LOGGER.error(CIRCULAR_DEPENDENCIES_ERROR_SEPARATOR);
                throw new CircularModuleDependenciesException();
            }

            this.processModule(dependencyModule, ModuleUtils.concat(dependants, List.of(clazz)));

            if (required && !this.orderedEntries.containsKey(dependencyClass)) {
                LOGGER.error(
                        "Aborting processing module {} because one of its required dependencies ({}) failed to process",
                        clazz.getSimpleName(),
                        specifiedDependencyClass.getSimpleName()
                );
                return false;
            }
        }

        return true;
    }

    private static void logDependencyFailed(Class<?> dependantClass, Class<?> dependencyClass) {
        LOGGER.error(
                "Module {} required by module {} was not registered or failed to process",
                dependencyClass.getSimpleName(),
                dependantClass.getSimpleName()
        );
    }
}