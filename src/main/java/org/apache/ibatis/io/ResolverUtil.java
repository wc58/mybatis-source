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
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * 加载包下的指定类
 * @param <T>
 */
public class ResolverUtil<T> {

    /**
     * 日志记录
     */
    private static final Log log = LogFactory.getLog(ResolverUtil.class);

    /**
     * 根据条件来匹配
     */
    public interface Test {

        /**
         * 指定匹配的条件
         * @param type
         * @return
         */
        boolean matches(Class<?> type);
    }

    /**
     * 根据类型进行匹配
     */
    public static class IsA implements Test {

        /**
         * 匹配的类型
         */
        private Class<?> parent;

        /**
         * 创建该对象时，就确定了类型
         * @param parentType
         */
        public IsA(Class<?> parentType) {
            this.parent = parentType;
        }

        /**
         * 判断扫描出的类是否和匹配的类型一致
         * @param type
         * @return
         */
        @Override
        public boolean matches(Class<?> type) {
            return type != null && parent.isAssignableFrom(type);
        }

        @Override
        public String toString() {
            return "is assignable to " + parent.getSimpleName();
        }
    }

    /**
     * 根据注解匹配
     */
    public static class AnnotatedWith implements Test {

        /**
         * 匹配的注解
         */
        private Class<? extends Annotation> annotation;

        /**
         * 创建该对象时，就确定了注解
         * @param annotation
         */
        public AnnotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }

        /**
         * 判断扫描出的类是否了加指定注解
         * @param type
         * @return
         */
        @Override
        public boolean matches(Class<?> type) {
            return type != null && type.isAnnotationPresent(annotation);
        }

        @Override
        public String toString() {
            return "annotated with @" + annotation.getSimpleName();
        }
    }

    /**
     * 存放已经扫描出来的类
     */
    private Set<Class<? extends T>> matches = new HashSet<>();

    /**
     * 所使用的类加载器
     */
    private ClassLoader classloader;

    /**
     * 返回已经扫描出来的类
     * @return
     */
    public Set<Class<? extends T>> getClasses() {
        return matches;
    }

    /**
     * 返回类加载器
     *  没有则使用当前线程中的类加载器
     * @return
     */
    public ClassLoader getClassLoader() {
        return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
    }

    /**
     * 指定类加载器
     * @param classloader
     */
    public void setClassLoader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    /**
     * 扫描包下的指定类型的类
     * @param parent
     * @param packageNames
     * @return
     */
    public ResolverUtil<T> findImplementations(Class<?> parent, String... packageNames) {
        if (packageNames == null) {
            return this;
        }

        Test test = new IsA(parent);
        for (String pkg : packageNames) {
            find(test, pkg);
        }

        return this;
    }

    /**
     * 扫描包下加指定注解的类
     * @param annotation
     * @param packageNames
     * @return
     */
    public ResolverUtil<T> findAnnotated(Class<? extends Annotation> annotation, String... packageNames) {
        if (packageNames == null) {
            return this;
        }

        Test test = new AnnotatedWith(annotation);
        for (String pkg : packageNames) {
            find(test, pkg);
        }

        return this;
    }

    /**
     * 获取包路径下的所有类的路径
     * 进行匹配保存
     * @param test
     * @param packageName
     * @return
     */
    public ResolverUtil<T> find(Test test, String packageName) {
        String path = getPackagePath(packageName);

        try {
            List<String> children = VFS.getInstance().list(path);
            for (String child : children) {
                if (child.endsWith(".class")) {
                    addIfMatching(test, child);
                }
            }
        } catch (IOException ioe) {
            log.error("Could not read package: " + packageName, ioe);
        }

        return this;
    }

    /**
     * 包名转为包路径
     * @param packageName
     * @return
     */
    protected String getPackagePath(String packageName) {
        return packageName == null ? null : packageName.replace('.', '/');
    }

    /**
     * 去掉后缀名，将类路径转换为全类名
     * 通过类加载器方式加载类
     * 判断是否指定类型的
     * 最后放入到容器中
     * @param test
     * @param fqn
     */
    @SuppressWarnings("unchecked")
    protected void addIfMatching(Test test, String fqn) {
        try {
            String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
            ClassLoader loader = getClassLoader();
            if (log.isDebugEnabled()) {
                log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");
            }

            Class<?> type = loader.loadClass(externalName);
            if (test.matches(type)) {
                matches.add((Class<T>) type);
            }
        } catch (Throwable t) {
            log.warn("Could not examine class '" + fqn + "'" + " due to a "
                    + t.getClass().getName() + " with message: " + t.getMessage());
        }
    }
}
