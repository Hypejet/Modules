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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A test of getting abstract modules, their superclass modules, and modules extending them.
 *
 * @since 1.0
 * @see AbstractModule
 */
@NullMarked
public final class AbstractModulesTest {
    @Test
    public void testGetting() {
        ModuleTests.test(
                registry -> registry.register(ModuleD1.class, ModuleD1::new).register(ModuleB2.class, ModuleB2::new),
                manager -> {
                    Assertions.assertInstanceOf(ModuleB2.class, manager.getModule(ModuleA.class));
                    Assertions.assertNull(manager.getModule(ModuleB1.class));
                    Assertions.assertInstanceOf(ModuleD1.class, manager.getModule(ModuleC1.class));
                }
        );
    }

    private static abstract class ModuleA extends Module<ModuleTestEnvironment> {}

    @AbstractModule
    private static abstract class ModuleB1 extends ModuleA {}

    private static abstract class ModuleC1 extends ModuleB1 {}

    private static final class ModuleD1 extends ModuleC1 {}

    private static final class ModuleB2 extends ModuleA {}
}