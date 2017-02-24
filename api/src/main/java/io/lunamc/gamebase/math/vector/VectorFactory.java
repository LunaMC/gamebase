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

package io.lunamc.gamebase.math.vector;

import java.util.Objects;

public interface VectorFactory {

    default Vector3f createVector3f() {
        return createVector3f(0, 0, 0);
    }

    default Vector3f createVector3f(Vector3f vector) {
        Objects.requireNonNull(vector, "vector must not be null");
        return createVector3f(vector.getX(), vector.getY(), vector.getZ());
    }

    Vector3f createVector3f(float x, float y, float z);
}
