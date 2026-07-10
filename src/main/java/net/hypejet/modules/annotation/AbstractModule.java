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
package net.hypejet.modules.annotation;

import net.hypejet.modules.ModuleManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used to specify that modules extending the annotated class
 * cannot be directly referenced via that class and all of its superclasses.
 *
 * <p>This means that it is allowed to register multiple modules extending the same
 * class - which is annotated with this annotation - in the same module registry.</p>
 *
 * <p>However, as the definition makes clear, this annotation disables the ability to use the annotated class
 * and all of its superclasses to reference subclass modules of the annotated class in {@link ModuleDependencies},
 * {@link ModuleManager#getModule(Class)}, as well as {@link ModuleManager#isModuleLoaded(Class)}, though the list
 * may be incomplete.</p>
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AbstractModule {}