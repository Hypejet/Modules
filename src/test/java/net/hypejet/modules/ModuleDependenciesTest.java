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
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * A test of loading modules that depend on other modules and order of such loading.
 *
 * @since 1.0
 * @see Module
 */
@NullMarked
public final class ModuleDependenciesTest {
    @Test
    public void testLoadingAndOrder() {
        ModuleManager<ModuleTestEnvironment> moduleManager = new ModuleRegistry<ModuleTestEnvironment>()
                .register(ModuleA.class, ModuleA::new)
                .register(ModuleB.class, ModuleB::new)
                .register(ModuleC.class, ModuleC::new)
                .register(ModuleD.class, ModuleD::new)
                .register(ModuleE.class, ModuleE::new)
                .register(ModuleF.class, ModuleF::new)
                .register(NOOPSucceedingModule.class, NOOPSucceedingModule::new)
                .register(NOOPFailingModule.class, NOOPFailingModule::new)
                .createModuleManager(ModuleTestEnvironment.INSTANCE);

        moduleManager.load();

        ModuleA moduleA = ModuleTests.assertModuleLoaded(ModuleA.class, moduleManager);
        ModuleB moduleB = ModuleTests.assertModuleLoaded(ModuleB.class, moduleManager);
        ModuleC moduleC = ModuleTests.assertModuleLoaded(ModuleC.class, moduleManager);
        ModuleD moduleD = ModuleTests.assertModuleLoaded(ModuleD.class, moduleManager);
        ModuleF moduleF = ModuleTests.assertModuleLoaded(ModuleF.class, moduleManager);

        ModuleTests.assertModuleLoaded(NOOPSucceedingModule.class, moduleManager);
        ModuleTests.assertModuleNotLoaded(NOOPFailingModule.class, moduleManager);
        ModuleTests.assertModuleNotLoaded(ModuleE.class, moduleManager);

        moduleManager.unload();

        moduleA.assertLoadedState(ModuleB.class, true);
        moduleA.assertLoadedState(ModuleC.class, true);
        moduleA.assertLoadedModulesEqual();

        moduleB.assertLoadedState(ModuleA.class, false);
        moduleB.assertLoadedState(ModuleC.class, true);
        moduleB.assertLoadedModulesEqual();

        moduleC.assertLoadedState(ModuleA.class, false);
        moduleC.assertLoadedState(ModuleB.class, false);
        moduleC.assertLoadedState(ModuleD.class, false);
        moduleC.assertLoadedModulesEqual();

        moduleD.assertLoadedState(ModuleC.class, true);
        moduleD.assertLoadedModulesEqual();

        moduleF.assertLoadedState(ModuleD.class, true);
        moduleF.assertLoadedState(ModuleE.class, false);
        moduleF.assertLoadedModulesEqual();
    }

    @Test
    public void testRemovedDependenciesWithDependencyModule() {
        testRemovedDependencies(true);
    }

    @Test
    public void testRemovedDependenciesWithoutDependencyModule() {
        testRemovedDependencies(false);
    }

    private static void testRemovedDependencies(boolean registerFailingModule) {
        ModuleTests.test(
                registry -> {
                    registry.register(ModuleH.class, ModuleH::new);
                    if (!registerFailingModule) return;
                    registry.register(NOOPFailingModule.class, NOOPFailingModule::new);
                },
                manager -> {
                    ModuleTests.assertModuleLoaded(ModuleH.class, manager);
                    ModuleTests.assertModuleLoaded(ModuleG.class, manager);
                    ModuleTests.assertModuleNotLoaded(NOOPFailingModule.class, manager);
                }
        );
    }

    @ModuleDependencies(required = ModuleB.class, optional = ModuleC.class)
    private static final class ModuleA extends LoadHistoryModule {}

    @ModuleDependencies(required = ModuleC.class)
    private static final class ModuleB extends LoadHistoryModule {}

    private static final class ModuleC extends LoadHistoryModule {}

    @ModuleDependencies(optional = ModuleC.class)
    private static final class ModuleD extends LoadHistoryModule {}

    @ModuleDependencies(required = NOOPFailingModule.class)
    private static final class ModuleE extends LoadHistoryModule {}

    @ModuleDependencies(required = ModuleD.class, optional = ModuleE.class)
    private static final class ModuleF extends LoadHistoryModule {}

    @ModuleDependencies(required = NOOPFailingModule.class)
    private static abstract class ModuleG extends Module<ModuleTestEnvironment> {}

    @ModuleDependencies(removed = NOOPFailingModule.class)
    private static final class ModuleH extends ModuleG {}

    @AbstractModule
    private static abstract class LoadHistoryModule extends Module<ModuleTestEnvironment> {

        private final Set<Class<?>> loadedOnLoad = new HashSet<>();
        private final Set<Class<?>> loadedOnUnload = new HashSet<>();

        protected void assertLoadedState(Class<?> clazz, boolean loaded) {
            this.assertLoadedState(clazz, loaded, true);
            this.assertLoadedState(clazz, loaded, false);
        }

        protected void assertLoadedModulesEqual() {
            Assertions.assertEquals(
                    this.loadedOnLoad, this.loadedOnUnload,
                    String.format(
                            "Module manager had different modules loaded while %s was loading and unloading",
                            this.getClass().getSimpleName()
                    )
            );
        }

        @Override
        protected void load() {
            this.handleLoadState(true);
        }

        @Override
        protected void unload() {
            this.handleLoadState(false);
        }

        private void handleLoadState(boolean loaded) {
            this.getModuleManager().getModules().forEach(module -> this.getSet(loaded).add(module.getClass()));
        }

        private boolean wasLoaded(Class<?> clazz, boolean onLoad) {
            return this.getSet(onLoad).contains(clazz);
        }

        private void assertLoadedState(Class<?> clazz, boolean loaded, boolean onLoad) {
            Assertions.assertEquals(
                    loaded, this.wasLoaded(clazz, onLoad),
                    String.format(
                            "%s expected %s to be%s loaded while %s",
                            this.getClass().getSimpleName(), clazz.getSimpleName(),
                            loaded ? "" : " NOT", onLoad ? "loading" : "unloading"
                    )
            );
        }

        private Set<Class<?>> getSet(boolean onLoad) {
            return onLoad ? this.loadedOnLoad : this.loadedOnUnload;
        }
    }
}