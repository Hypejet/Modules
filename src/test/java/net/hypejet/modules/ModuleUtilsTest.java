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
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A test of internal utilities used by the library.
 *
 * @since 1.0
 */
@NullMarked
public final class ModuleUtilsTest {
    @Test
    public void testEmptySuperclassModules() {
        assertNoSuperclassModules(NOOPSucceedingModule.class);
    }

    @Test
    public void testEmptySuperclassModulesWithAbstract() {
        assertNoSuperclassModules(ModuleF.class);
    }

    @Test
    public void testOneSuperclassModule() {
        assertSuperclassModulesEquals(ModuleC.class, ModuleD.class);
    }

    @Test
    public void testOneSuperclassModuleWithAbstract() {
        assertSuperclassModulesEquals(ModuleG.class, ModuleF.class);
    }

    @Test
    public void testMultipleSuperclassModules() {
        assertSuperclassModulesEquals(ModuleA.class, ModuleB.class, ModuleC.class, ModuleD.class);
    }

    @Test
    public void testMultipleSuperclassModulesWithAbstract() {
        assertSuperclassModulesEquals(ModuleI.class, ModuleH.class, ModuleG.class, ModuleF.class);
    }

    @Test
    public void testConcat() {
        Assertions.assertEquals(
                List.of("abcdefg", "qwerty", "12345", "54216", "i-like-hypejet-modules", "Hypejet ftw"),
                ModuleUtils.concat(
                        List.of("abcdefg", "qwerty"),
                        List.of("12345"),
                        List.of("54216", "i-like-hypejet-modules", "Hypejet ftw")
                )
        );
    }

    private static void assertNoSuperclassModules(Class<?> clazz) {
        ModuleUtils.forEachSuperclassModule(clazz, _ -> {
            throw new AssertionFailedError(String.format(
                    "Expected %s to have no superclass modules",
                    clazz.getSimpleName()
            ));
        });
    }

    private static void assertSuperclassModulesEquals(Class<?> clazz, Class<?>... expectedSuperclasses) {
        AtomicInteger index = new AtomicInteger();

        ModuleUtils.forEachSuperclassModule(clazz, superclass -> {
            Assertions.assertTrue(index.get() < expectedSuperclasses.length, "Encountered more superclass modules than expected");
            Assertions.assertEquals(expectedSuperclasses[index.getAndIncrement()], superclass);
        });
    }

    private static class ModuleA extends ModuleB {}

    private static class ModuleB extends ModuleC {}

    private static class ModuleC extends ModuleD {}

    private static abstract class ModuleD extends Module<ModuleTestEnvironment> {}

    @AbstractModule
    private static abstract class ModuleE extends ModuleD {}

    private static class ModuleF extends ModuleE {}

    private static class ModuleG extends ModuleF {}

    private static class ModuleH extends ModuleG {}

    private static class ModuleI extends ModuleH {}
}