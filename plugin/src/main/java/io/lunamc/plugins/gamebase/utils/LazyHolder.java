/*
 *  Copyright 2017 LunaMC.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.lunamc.plugins.gamebase.utils;

import java.util.function.Supplier;

public class LazyHolder<T> {

    private volatile Supplier<T> supplier;
    private T value;

    private LazyHolder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T getValue() {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;
        }
        return value;
    }

    public static <T> LazyHolder<T> create(Supplier<T> supplier) {
        return new LazyHolder<>(supplier);
    }
}
