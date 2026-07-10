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

import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.function.Supplier;

@NullMarked
record ModuleEntry<E, M extends Module<? super E>>(Class<M> clazz, Supplier<M> supplier,
                                                   Set<Class<?>> requiredDependencies,
                                                   Set<Class<?>> optionalDependencies) {
    ModuleEntry {
        requiredDependencies = Set.copyOf(requiredDependencies);
        optionalDependencies = Set.copyOf(optionalDependencies);
    }
}