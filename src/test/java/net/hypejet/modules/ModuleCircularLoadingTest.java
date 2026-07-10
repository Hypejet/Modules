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

import net.hypejet.modules.annotation.ModuleDependencies;
import net.hypejet.modules.exception.CircularModuleDependenciesException;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

/**
 * A test of creating module managers containing circular module dependencies.
 *
 * @since 1.0
 * @see ModuleManager
 */
@NullMarked
public final class ModuleCircularLoadingTest {
    @Test
    public void testOptional() {
        assertCircularLoading(registry -> registry.register(ModuleA.class, ModuleA::new)
                .register(ModuleB.class, ModuleB::new)
                .register(ModuleC.class, ModuleC::new)
                .register(ModuleD.class, ModuleD::new));
    }

    @Test
    public void testRequired() {
        assertCircularLoading(registry -> registry
                .register(ModuleE.class, ModuleE::new)
                .register(ModuleF.class, ModuleF::new));
    }

    @Test
    public void testDirectOptional() {
        assertCircularLoading(registry -> registry.register(DirectOptional.class, DirectOptional::new));
    }

    @Test
    public void testDirectRequired() {
        assertCircularLoading(registry -> registry.register(DirectRequired.class, DirectRequired::new));
    }

    private static void assertCircularLoading(Consumer<ModuleRegistry<ModuleTestEnvironment>> registryAction) {
        ModuleRegistry<ModuleTestEnvironment> registry = new ModuleRegistry<>();
        registryAction.accept(registry);

        Assertions.assertThrowsExactly(
                CircularModuleDependenciesException.class,
                () -> registry.createModuleManager(ModuleTestEnvironment.INSTANCE)
        );
    }

    @ModuleDependencies(required = ModuleB.class)
    private static final class ModuleA extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }

    @ModuleDependencies(required = ModuleC.class)
    private static final class ModuleB extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }

    @ModuleDependencies(optional = ModuleD.class)
    private static final class ModuleC extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }

    @ModuleDependencies(required = ModuleA.class)
    private static final class ModuleD extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }

    @ModuleDependencies(required = ModuleF.class)
    private static final class ModuleE extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }

    @ModuleDependencies(required = ModuleE.class)
    private static final class ModuleF extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }

    @ModuleDependencies(optional = DirectOptional.class)
    private static final class DirectOptional extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }

    @ModuleDependencies(required = DirectRequired.class)
    private static final class DirectRequired extends Module<ModuleTestEnvironment> {
        @Override
        protected void load() {}

        @Override
        protected void unload() {}
    }
}