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
import net.hypejet.modules.annotation.ModuleDependencies;
import net.hypejet.modules.exception.CircularModuleDependenciesException;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A registry of modules to be created and loaded by a {@link ModuleManager}.
 *
 * @param <E> the type of environment that the module manager is going to run on
 * @since 1.0
 * @see ModuleManager
 */
@NullMarked
public final class ModuleRegistry<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleRegistry.class);

    private final Map<Class<? extends Module<? super E>>, ModuleEntry<E, ?>> moduleEntries = new HashMap<>();
    private final Map<Class<?>, Class<? extends Module<? super E>>> superclassModules = new HashMap<>();

    /**
     * Registers the specified module.
     *
     * <p>The specified supplier must create module instances that are
     * of <strong>the same</strong> class as the specified one.</p>
     *
     * @param clazz the class of the module to register
     * @param supplier the supplier of the module instance
     * @return this module registry
     * @param <M> the type of module that is being registered
     * @since 1.0
     */
    public <M extends Module<? super E>> ModuleRegistry<E> register(Class<M> clazz, Supplier<M> supplier) {
        if (this.moduleEntries.containsKey(clazz) || this.superclassModules.containsKey(clazz)) {
            LOGGER.error("Module with class {} is already registered", clazz.getSimpleName(), new Throwable());
            return this;
        }

        if (!Module.class.isAssignableFrom(clazz)) {
            LOGGER.error(
                    "Cannot register module with class {} because it is not a subclass of the {} class",
                    clazz.getSimpleName(),
                    Module.class.getSimpleName()
            );
            return this;
        }

        if (clazz.isAnnotationPresent(AbstractModule.class)) {
            LOGGER.error(
                    "Cannot register module with class {} because it was marked as an abstract module",
                    clazz.getSimpleName()
            );
            return this;
        }

        Class<?> superclass = clazz.getSuperclass();
        while (!superclass.isAnnotationPresent(AbstractModule.class)) {
            if (!this.superclassModules.containsKey(superclass)) {
                superclass = superclass.getSuperclass();
                continue;
            }

            LOGGER.error(
                    "Cannot register module {} because there already is another registered module extending class {}",
                    clazz.getSimpleName(),
                    superclass.getSimpleName()
            );

            return this;
        }

        Set<Class<?>> requiredDependencies = new HashSet<>();
        Set<Class<?>> optionalDependencies = new HashSet<>();

        if (putDependencies(clazz, requiredDependencies, optionalDependencies)) {
            LOGGER.error("Cannot register module with class {} because one of its required dependencies is invalid", clazz.getSimpleName());
            return this;
        }

        this.moduleEntries.put(clazz, new ModuleEntry<>(clazz, supplier, requiredDependencies, optionalDependencies));
        ModuleUtils.forEachSuperclassModule(clazz, superclassModule -> this.superclassModules.put(superclassModule, clazz));
        return this;
    }

    /**
     * Creates a {@link ModuleManager} using the specified environment and modules registered in this module registry.
     *
     * @param environment the environment that that module manager should run on
     * @return the created module manager
     * @throws CircularModuleDependenciesException if the module manager could not be created
     *                                             because circular module dependencies were detected
     * @since 1.0
     */
    public ModuleManager<E> createModuleManager(E environment) {
        ModuleLoadOrder<E> loadOrder = new ModuleLoadOrder<>(this.moduleEntries, this.superclassModules);
        loadOrder.process();
        return new ModuleManager<>(environment, loadOrder.getResult());
    }

    private static boolean putDependencies(Class<?> clazz, Set<Class<?>> requiredDependencies, Set<Class<?>> optionalDependencies) {
        ModuleDependencies dependenciesAnnotation = clazz.getAnnotation(ModuleDependencies.class);

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && putDependencies(superclass, requiredDependencies, optionalDependencies))
            return true;

        if (dependenciesAnnotation == null) return false;

        Set<Class<?>> singleClassRequiredDependencies = new HashSet<>();
        for (Class<?> required : dependenciesAnnotation.required()) {
            if (isInvalidDependency(clazz, required, DependencyType.REQUIRED)) return true;
            singleClassRequiredDependencies.add(required);
            requiredDependencies.add(required);
            optionalDependencies.remove(required);
        }

        Set<Class<?>> singleClassOptionalDependencies = new HashSet<>();
        for (Class<?> optional : dependenciesAnnotation.optional()) {
            if (singleClassRequiredDependencies.contains(optional)) {
                LOGGER.warn(
                        "{} was specified both as a required and optional dependency by class {}",
                        optional.getSimpleName(),
                        clazz.getSimpleName()
                );
                continue;
            }

            if (isInvalidDependency(clazz, optional, DependencyType.OPTIONAL)) continue;
            singleClassOptionalDependencies.add(optional);
            requiredDependencies.remove(optional);
            optionalDependencies.add(optional);
        }

        for (Class<?> removed : dependenciesAnnotation.removed()) {
            if (singleClassRequiredDependencies.contains(removed) || singleClassOptionalDependencies.contains(removed)) {
                LOGGER.warn(
                        "{} was specified both as a required/optional and removed dependency by class {}",
                        removed.getSimpleName(),
                        clazz.getSimpleName()
                );
                continue;
            }

            if (isInvalidDependency(clazz, removed, DependencyType.REMOVED)) continue;
            requiredDependencies.remove(removed);
            optionalDependencies.remove(removed);
        }

        return false;
    }

    private static boolean isInvalidDependency(Class<?> clazz, Class<?> dependencyClass, DependencyType type) {
        boolean required = type == DependencyType.REQUIRED;

        if (!Module.class.isAssignableFrom(dependencyClass)) {
            LOGGER.atLevel(required ? Level.ERROR : Level.WARN).log(
                    "Class {} specified {} as {} dependency, while it is not a module class",
                    clazz.getSimpleName(),
                    dependencyClass.getSimpleName(),
                    type.logDependencyType
            );
            return true;
        }

        if (dependencyClass.isAnnotationPresent(AbstractModule.class)) {
            LOGGER.atLevel(required ? Level.ERROR : Level.WARN).log(
                    "Class {} specified an abstract module ({}) as {} dependency, which is not allowed",
                    clazz.getSimpleName(),
                    dependencyClass.getSimpleName(),
                    type.logDependencyType
            );
            return true;
        }

        return false;
    }

    private enum DependencyType {
        REQUIRED("a required"),
        OPTIONAL("an optional"),
        REMOVED("a removed");

        private final String logDependencyType;

        DependencyType(String logDependencyType) {
            this.logDependencyType = logDependencyType;
        }
    }
}