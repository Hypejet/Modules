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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

/**
 * A manager of modules.
 *
 * @param <E> the type of environment that this module manager runs on
 * @since 1.0
 * @see Module
 */
@NullMarked
public final class ModuleManager<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);

    private final E environment;
    private final List<ModuleEntry<E, ?>> loadOrder;

    private final SequencedMap<Class<?>, Module<? super E>> modules = new LinkedHashMap<>();
    private final Map<Class<?>, Module<? super E>> superclassModules = new HashMap<>();

    private State state = State.CREATED;

    ModuleManager(E environment, List<ModuleEntry<E, ?>> loadOrder) {
        this.environment = environment;
        this.loadOrder = loadOrder;
    }

    /**
     * Gets the environment that this module manager runs on.
     *
     * @return the environment
     * @throws IllegalStateException if this module manager is not loaded
     * @since 1.0
     */
    public E getEnvironment() {
        this.ensureLoaded();
        return this.environment;
    }

    /**
     * Gets a module instance with the specified class.
     *
     * <p>If there is no module directly registered with the specified class, this method tries to find a module
     * whose one of superclasses is the specified module class. However, this does not apply for superclasses
     * that are annotated with {@link AbstractModule}. See {@link AbstractModule} for more information.</p>
     *
     * @param clazz the class of the desired module instance
     * @return the module instance, {@code null} if the module was not loaded
     * @param <M> the type of module instance to return
     * @throws IllegalStateException if this module manager is not loaded
     * @since 1.0
     * @see AbstractModule
     */
    public <M extends Module<?>> @Nullable M getModule(Class<M> clazz) {
        this.ensureLoaded();
        return clazz.cast(this.modules.getOrDefault(clazz, this.superclassModules.get(clazz)));
    }

    /**
     * Gets a module instance with the specified class. Throws an exception if it is not loaded.
     *
     * @param clazz the class of the desired module instance
     * @return the module instance
     * @param <M> the type of module instance to return
     * @throws IllegalStateException if either this module manager or the specified module is not loaded
     * @since 1.0
     * @see #getModule(Class)
     */
    public <M extends Module<?>> M getModuleOrThrow(Class<M> clazz) {
        M module = this.getModule(clazz);
        if (module != null) return module;
        throw new IllegalStateException("Module with class " + clazz.getSimpleName() + " is not loaded");
    }

    /**
     * Gets a collection of currently loaded modules.
     *
     * @return the modules that are currently loaded
     * @throws IllegalStateException if this module manager is not loaded
     * @since 1.0
     */
    public Collection<? extends Module<? super E>> getModules() {
        this.ensureLoaded();
        return List.copyOf(this.modules.values());
    }

    /**
     * Gets whether a module with the specified class was loaded.
     *
     * <p>If there is no module directly registered with the specified class, this method tries to find a module
     * whose one of superclasses is the specified module class. However, this does not apply for superclasses
     * that are annotated with {@link AbstractModule}. See {@link AbstractModule} for more information.</p>
     *
     * @param clazz the class of the module to check
     * @return {@code true} if the module was loaded, {@code false} otherwise
     * @throws IllegalStateException if this module manager is not loaded
     * @since 1.0
     * @see AbstractModule
     */
    public boolean isModuleLoaded(Class<? extends Module<?>> clazz) {
        return this.getModule(clazz) != null;
    }

    /**
     * Loads all modules created by this module manager.
     *
     * @throws IllegalStateException if this module manager is unloaded
     * @since 1.0
     */
    public void load() {
        if (this.state == State.UNLOADED)
            throw new IllegalStateException("The module manager is unloaded");

        if (this.isLoaded()) {
            LOGGER.error("The module manager is already loaded", new Throwable());
            return;
        }

        this.state = State.LOADING;

        this.loadOrder.removeIf(entry -> {
            this.createAndLoadModule(entry);
            return true;
        });

        this.state = State.LOADED;
    }

    /**
     * Unloads all modules loaded by this module manager.
     *
     * @throws IllegalStateException if this module manager is not loaded
     * @since 1.0
     */
    public void unload() {
        this.ensureLoaded();

        if (this.state == State.LOADING) {
            LOGGER.error("You cannot unload the module manager before it gets fully loaded", new Throwable());
            return;
        } else if (this.state == State.UNLOADING) {
            LOGGER.error("The module manager is already unloading", new Throwable());
            return;
        }

        this.state = State.UNLOADING;

        this.modules.sequencedValues().reversed().removeIf(module -> {
            this.tryUnload(module);
            return true;
        });

        this.state = State.UNLOADED;
    }

    private <M extends Module<? super E>> void createAndLoadModule(ModuleEntry<E, M> entry) {
        Class<M> clazz = entry.clazz();

        try {
            for (Class<?> dependencyClass : entry.requiredDependencies()) {
                if (this.modules.containsKey(dependencyClass) || this.superclassModules.containsKey(dependencyClass))
                    continue;

                LOGGER.error(
                        "Cannot create module with class {} because one of its required dependencies ({}) was not loaded",
                        clazz.getSimpleName(),
                        dependencyClass.getSimpleName()
                );

                return;
            }

            M module = entry.supplier().get();
            Class<?> actualClass = module.getClass();

            if (!clazz.equals(actualClass)) {
                LOGGER.error(
                        "Module with class {} was registered using another class ({})",
                        actualClass.getSimpleName(),
                        clazz.getSimpleName()
                );
                return;
            }

            this.modules.put(clazz, module);
            ModuleUtils.forEachSuperclassModule(clazz, superclass -> this.superclassModules.put(superclass, module));

            try {
                module.setModuleManager(this);
                module.load();
            } catch (Throwable throwable) {
                this.tryUnload(module);
                this.modules.remove(clazz);
                LOGGER.error("An error occurred while loading module with class {}", clazz.getSimpleName(), throwable);
            }
        } catch (Throwable throwable) {
            LOGGER.error("An error occurred while creating and loading module with class {}", clazz.getSimpleName(), throwable);
        }
    }

    private void tryUnload(Module<? super E> module) {
        try {
            module.unload();
        } catch (Throwable throwable) {
            LOGGER.error("An error occurred while unloading a module", throwable);
        } finally {
            module.setModuleManager(null);
            ModuleUtils.forEachSuperclassModule(module.getClass(), this.superclassModules::remove);
        }
    }

    private boolean isLoaded() {
        return this.state == State.LOADING || this.state == State.LOADED || this.state == State.UNLOADING;
    }

    private void ensureLoaded() {
        if (this.isLoaded()) return;
        throw new IllegalStateException("The module manager is not loaded");
    }

    private enum State {
        CREATED,
        LOADING,
        LOADED,
        UNLOADING,
        UNLOADED
    }
}