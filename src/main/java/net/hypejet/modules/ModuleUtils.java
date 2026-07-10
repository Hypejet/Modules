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

import net.hypejet.modules.annotation.AbstractModule;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@NullMarked
final class ModuleUtils {

    private ModuleUtils() {}

    static void forEachSuperclassModule(Class<?> moduleClass, Consumer<Class<?>> action) {
        Class<?> superclass = moduleClass.getSuperclass();
        while (!superclass.isAnnotationPresent(AbstractModule.class)) {
            action.accept(superclass);
            superclass = superclass.getSuperclass();
        }
    }

    @SafeVarargs
    static <E> List<E> concat(List<? extends E>... lists) {
        List<E> newList = new ArrayList<>();
        for (List<? extends E> list : lists)
            newList.addAll(list);
        return List.copyOf(newList);
    }
}