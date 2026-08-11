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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A test of module manager state after loading modules.
 *
 * @since 1.0.0
 * @see Module
 * @see ModuleManager
 */
@NullMarked
public final class ModuleLoadingTest {
    @Test
    public void testSuccessfulLoading() {
        testSingleModule(NOOPSucceedingModule.class, NOOPSucceedingModule::new, manager -> {
            NOOPSucceedingModule module = ModuleTests.assertModuleLoaded(NOOPSucceedingModule.class, manager);
            Assertions.assertIterableEquals(manager.getModules(), List.of(module));
        });
    }

    @Test
    public void testFailingLoading() {
        testSingleModule(NOOPFailingModule.class, NOOPFailingModule::new, manager -> {
            ModuleTests.assertModuleNotLoaded(NOOPFailingModule.class, manager);
            Assertions.assertIterableEquals(manager.getModules(), List.of());
        });
    }

    @Test
    public void testMixedLoading() {
        ModuleTests.test(
                registry -> registry
                        .register(NOOPSucceedingModule.class, NOOPSucceedingModule::new)
                        .register(NOOPFailingModule.class, NOOPFailingModule::new),
                manager -> {
                    ModuleTests.assertModuleLoaded(NOOPSucceedingModule.class, manager);
                    ModuleTests.assertModuleNotLoaded(NOOPFailingModule.class, manager);
                }
        );
    }

    private static <M extends Module<ModuleTestEnvironment>> void testSingleModule(Class<M> moduleClass, Supplier<M> moduleSupplier,
                                                                                   Consumer<ModuleManager<ModuleTestEnvironment>> action) {
        ModuleTests.test(registry -> registry.register(moduleClass, moduleSupplier), action);
    }
}