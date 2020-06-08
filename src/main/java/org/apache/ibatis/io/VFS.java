/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.io;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * 加载资源
 */
public abstract class VFS {
    //日志
    private static final Log log = LogFactory.getLog(VFS.class);

    //VFS内置的实现
    public static final Class<?>[] IMPLEMENTATIONS = {JBoss6VFS.class, DefaultVFS.class};

    //用户实现的VFS
    public static final List<Class<? extends VFS>> USER_IMPLEMENTATIONS = new ArrayList<>();

    /**
     * 保证VFS是单例的
     */
    private static class VFSHolder {
        //保存实例
        static final VFS INSTANCE = createVFS();

        /**
         * 实例化VFS
         * @return
         */
        @SuppressWarnings("unchecked")
        static VFS createVFS() {
            //首先尝试用户，再是内置
            List<Class<? extends VFS>> impls = new ArrayList<>();
            impls.addAll(USER_IMPLEMENTATIONS);
            impls.addAll(Arrays.asList((Class<? extends VFS>[]) IMPLEMENTATIONS));

            //遍历每个类，直到找到有效的
            VFS vfs = null;
            for (int i = 0; vfs == null || !vfs.isValid(); i++) {
                Class<? extends VFS> impl = impls.get(i);
                try {
                    //创建VFS实例
                    vfs = impl.getDeclaredConstructor().newInstance();
                    if (!vfs.isValid() && log.isDebugEnabled()) {
                        log.debug("VFS implementation " + impl.getName()
                                + " is not valid in this environment.");
                    }
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    log.error("Failed to instantiate " + impl, e);
                    return null;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Using VFS adapter " + vfs.getClass().getName());
            }

            return vfs;
        }
    }

    /**
     * 获取VFS实例（单例）
     * @return
     */
    public static VFS getInstance() {
        return VFSHolder.INSTANCE;
    }

    /**
     * 保存用户的VFS
     * @param clazz
     */
    public static void addImplClass(Class<? extends VFS> clazz) {
        if (clazz != null) {
            USER_IMPLEMENTATIONS.add(clazz);
        }
    }

    /**
     * 通过全类名反射加载类
     * @param className
     * @return
     */
    protected static Class<?> getClass(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
            // return ReflectUtil.findClass(className);
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Class not found: " + className);
            }
            return null;
        }
    }

    /**
     * 从指定类中查找指定方法对象
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    protected static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (SecurityException e) {
            log.error("Security exception looking for method " + clazz.getName() + "." + methodName + ".  Cause: " + e);
            return null;
        } catch (NoSuchMethodException e) {
            log.error("Method not found " + clazz.getName() + "." + methodName + "." + methodName + ".  Cause: " + e);
            return null;
        }
    }

    /**
     * 调用指定类的指定方法
     * @param method
     * @param object
     * @param parameters
     * @param <T>
     * @return
     * @throws IOException
     * @throws RuntimeException
     */
    @SuppressWarnings("unchecked")
    protected static <T> T invoke(Method method, Object object, Object... parameters)
            throws IOException, RuntimeException {
        try {
            return (T) method.invoke(object, parameters);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IOException) {
                throw (IOException) e.getTargetException();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将相对路径转化为url路径
     * @param path
     * @return
     * @throws IOException
     */
    protected static List<URL> getResources(String path) throws IOException {
        return Collections.list(Thread.currentThread().getContextClassLoader().getResources(path));
    }

    /**
     * 当前VFS是否有效
     * @return
     */
    public abstract boolean isValid();

    /**
     * 递归的加载出子资源
     * @param url
     * @param forPath
     * @return
     * @throws IOException
     */
    protected abstract List<String> list(URL url, String forPath) throws IOException;

    /**
     * 加载指定url下的文件
     * @param path
     * @return
     * @throws IOException
     */
    public List<String> list(String path) throws IOException {
        List<String> names = new ArrayList<>();
        for (URL url : getResources(path)) {
            names.addAll(list(url, path));
        }
        return names;
    }
}
