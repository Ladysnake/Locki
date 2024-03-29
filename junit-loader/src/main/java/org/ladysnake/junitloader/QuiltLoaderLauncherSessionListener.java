/*
 * Copyright 2016 FabricMC
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

package org.ladysnake.junitloader;

import net.fabricmc.api.EnvType;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.util.SystemProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class QuiltLoaderLauncherSessionListener implements LauncherSessionListener {
    static {
        System.setProperty(SystemProperties.DEVELOPMENT, "true");
    }

    private final ClassLoader classLoader;

    private ClassLoader launcherSessionClassLoader;

    public QuiltLoaderLauncherSessionListener() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        try {
            Knot knot = new Knot(EnvType.CLIENT);
            Method init = Knot.class.getDeclaredMethod("init", String[].class);
            init.setAccessible(true);
            classLoader = (ClassLoader) init.invoke(knot, (Object) new String[]{});
        } finally {
            // Knot.init sets the context class loader, revert it back for now.
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        final Thread currentThread = Thread.currentThread();
        launcherSessionClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(classLoader);
    }

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        final Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(launcherSessionClassLoader);
    }
}
