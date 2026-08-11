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
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A module to be loaded with {@link ModuleManager}.
 *
 * @param <E> the type of environment that this module runs on
 * @since 1.0
 * @see ModuleManager
 */
@NullMarked
@AbstractModule
public abstract class Module<E> {

    private @Nullable ModuleManager<? extends E> moduleManager;
    private @Nullable Logger logger;

    /**
     * Gets whether this module is currently loaded.
     *
     * @return {@code true} if the module is loaded, {@code false} otherwise
     * @since 1.0
     */
    public final boolean isLoaded() {
        return this.moduleManager != null;
    }

    /**
     * Gets the module manager that loaded this module.
     *
     * <p>This method can be safely called only if this module is loaded.</p>
     *
     * @return the module manager
     * @throws IllegalStateException if this module is not loaded
     * @since 1.0
     */
    public final ModuleManager<? extends E> getModuleManager() {
        if (!this.isLoaded())
            throw new IllegalStateException("The module is not loaded");
        return this.moduleManager;
    }

    /**
     * Gets the environment that this module runs on.
     *
     * <p>This is a shortcut for getting the module manager and returning the environment from it.</p>
     *
     * @return the environment
     * @throws IllegalStateException if this module is not loaded
     * @since 1.0
     * @see #getModuleManager()
     */
    public final E getEnvironment() {
        return this.getModuleManager().getEnvironment();
    }

    /**
     * Gets the logger of this module.
     *
     * @return the logger
     * @since 1.0.3
     */
    public final Logger getLogger() {
        if (this.logger == null)
            this.logger = LoggerFactory.getLogger(this.getClass());
        return this.logger;
    }

    /**
     * Called when this module becomes loaded.
     *
     * @since 1.0
     */
    @ApiStatus.OverrideOnly
    protected void load() {}

    /**
     * Called when this module becomes unloaded.
     *
     * @since 1.0
     */
    @ApiStatus.OverrideOnly
    protected void unload() {}

    final void setModuleManager(@Nullable ModuleManager<? extends E> moduleManager) {
        this.moduleManager = moduleManager;
    }
}