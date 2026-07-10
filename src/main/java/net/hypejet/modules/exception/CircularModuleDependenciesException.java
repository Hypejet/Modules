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
package net.hypejet.modules.exception;

import org.jetbrains.annotations.ApiStatus;

/**
 * An exception thrown when the module system detects circular module dependencies.
 *
 * @since 1.0
 * @see RuntimeException
 */
public final class CircularModuleDependenciesException extends RuntimeException {
    /**
     * Constructs the {@link CircularModuleDependenciesException}.
     *
     * <p><strong>For internal use only.</strong></p>
     *
     * @since 1.0
     */
    @ApiStatus.Internal
    public CircularModuleDependenciesException() {
        super("Circular module dependencies detected");
    }
}