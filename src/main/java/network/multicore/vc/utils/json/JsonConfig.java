package network.multicore.vc.utils.json;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright © 2019-2024 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public abstract class JsonConfig {

    /**
     * Use this method to initialize all the parameters of the config.
     */
    public abstract JsonConfig init();

    /**
     * This method reinitialize all the null parameters with the default value.
     *
     * @return true if there's at least a value reinitialized.
     */
    public boolean completeMissing() {
        boolean completed = false;

        for (Field field : getClass().getDeclaredFields()) {
            try {
                Object value = field.get(this);

                if (value == null) {
                    if (field.isAnnotationPresent(DefaultValue.class)) {
                        DefaultValue annotation = field.getAnnotation(DefaultValue.class);

                        Class<?> fieldType = field.getType();
                        if (fieldType == String.class) field.set(this, annotation.s());
                        else if (fieldType == int.class) field.set(this, annotation.i());
                        else if (fieldType == long.class) field.set(this, annotation.l());
                        else if (fieldType == double.class) field.set(this, annotation.d());
                        else if (fieldType == boolean.class) field.set(this, annotation.b());
                        else if (fieldType == String[].class) field.set(this, annotation.a());
                        else if (fieldType == List.class) field.set(this, Arrays.asList(annotation.a()));
                    }

                    completed = true;
                } else if (JsonConfig.class.isAssignableFrom(field.getType())) {
                    if (((JsonConfig) value).completeMissing()) completed = true;
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        if (completed) init();
        return completed;
    }
}
