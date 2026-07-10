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

import org.junit.jupiter.api.Assertions;

import java.util.function.Consumer;

final class ModuleTests {

    private ModuleTests() {}

    static <M extends Module<?>> M assertModuleLoaded(Class<M> moduleClass, ModuleManager<?> moduleManager) {
        M module = moduleManager.getModule(moduleClass);

        String expectedToBeLoadedMessage = moduleClass.getSimpleName() + " was expected to be loaded";
        Assertions.assertInstanceOf(moduleClass, module, expectedToBeLoadedMessage);
        Assertions.assertTrue(module.isLoaded(), expectedToBeLoadedMessage);
        Assertions.assertTrue(moduleManager.isModuleLoaded(moduleClass), expectedToBeLoadedMessage);

        return module;
    }

    static void assertModuleNotLoaded(Class<? extends Module<?>> moduleClass, ModuleManager<?> moduleManager) {
        String expectedToBeNotLoadedMessage = moduleClass.getSimpleName() + " was expected to be NOT loaded";
        Assertions.assertNull(moduleManager.getModule(moduleClass), expectedToBeNotLoadedMessage);
        Assertions.assertFalse(moduleManager.isModuleLoaded(moduleClass), expectedToBeNotLoadedMessage);
    }

    static void test(Consumer<ModuleRegistry<ModuleTestEnvironment>> registryAction,
                     Consumer<ModuleManager<ModuleTestEnvironment>> managerAction) {
        ModuleRegistry<ModuleTestEnvironment> registry = new ModuleRegistry<>();
        registryAction.accept(registry);

        ModuleManager<ModuleTestEnvironment> manager = registry.createModuleManager(ModuleTestEnvironment.INSTANCE);
        manager.load();
        managerAction.accept(manager);
        manager.unload();
    }
}