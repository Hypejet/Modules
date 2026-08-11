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

import net.hypejet.modules.Module;
import org.jspecify.annotations.NullMarked;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation specifying what modules should be enabled first before enabling the annotated module.
 *
 * <p>Specifying modules both as required, optional, and removed
 * is not supported, and those are considered required.</p>
 *
 * <p>If there is a dependency that could not be found directly with the specified class, then
 * the module manager tries to find a module whose class is a subclass of the specified class.</p>
 *
 * <p>However, classes annotated with {@link AbstractModule}, and all of their superclasses, cannot be used in this
 * annotation to reference subclass modules of abstract modules. See {@link AbstractModule} for more information.</p>
 *
 * <p>Furthermore, each module inherits dependencies from its superclasses, but whether dependencies are
 * required or not can be overridden by subclasses through specifying them again using this annotation.
 * It is even possible to remove a dependency by subclasses, see {@link #removed()} for more information.</p>
 *
 * @since 1.0.0
 * @see AbstractModule
 */
@NullMarked
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDependencies {
    /**
     * The modules that are required by the module annotated with this annotation.
     *
     * @return the classes of required modules
     * @since 1.0.0
     */
    Class<? extends Module>[] required() default {};

    /**
     * The modules that should be enabled before the module annotated
     * with this annotation if they are present, but are not strictly required.
     *
     * @return the classes of optional modules
     * @since 1.0.0
     */
    Class<? extends Module>[] optional() default {};

    /**
     * The modules that should not be considered as dependencies of the annotated module.
     *
     * <p>This is useful when superclasses consider certain module
     * a dependency, while the annotating class does not.</p>
     *
     * @return the classes of modules that are not dependencies
     * @since 1.0.0
     */
    Class<? extends Module>[] removed() default {};
}