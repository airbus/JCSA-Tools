/*
 * Copyright (c) 2026 Airbus Defence and Space Oy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airbus.esar.common;

import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;
import com.airbus.esar.soot.TypeUtils;

public final class JcTypeUtils {
    public static final String APPLET_FQN = "javacard.framework.Applet";

    private JcTypeUtils() {
    }

    /**
     * @param view       view to resolve intermediate classes
     * @param clazz      class to check
     * @param appletType resolved ClassType for {@code javacard.framework.Applet}
     * @return true if {@code clazz} is a concrete (non-abstract) subclass of Applet
     */
    public static boolean isConcreteApplet(final JavaView view,
                                           final JavaSootClass clazz,
                                           final ClassType appletType) {
        if (clazz.isAbstract() || clazz.getType().equals(appletType)) {
            return false;
        }
        return TypeUtils.extendsClass(view, clazz, appletType);
    }
}
