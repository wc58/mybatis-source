/**
 * Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * 所有建造器的抽象类
 */
public abstract class BaseBuilder {
    //主配置类
    protected final Configuration configuration;
    //别名注册器
    protected final TypeAliasRegistry typeAliasRegistry;
    //类型注册器
    protected final TypeHandlerRegistry typeHandlerRegistry;

    /**
     * 传入主配置类
     * 即可根据主配置类来取出，别名注册器、类型注册器
     * @param configuration
     */
    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    /**
     * 获取主配置类
     * @return
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 解析表达式
     * @param regex
     * @param defaultValue
     * @return
     */
    protected Pattern parseExpression(String regex, String defaultValue) {
        return Pattern.compile(regex == null ? defaultValue : regex);
    }

    /**
     * 字符串类型转为布尔类型
     * 不为空转换，否使用默认
     * @param value
     * @param defaultValue
     * @return
     */
    protected Boolean booleanValueOf(String value, Boolean defaultValue) {
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    /**
     * 字符串类型转为整数类型
     * 不为空转换，否使用默认
     * @param value
     * @param defaultValue
     * @return
     */
    protected Integer integerValueOf(String value, Integer defaultValue) {
        return value == null ? defaultValue : Integer.valueOf(value);
    }

    /**
     * 字符串类型转为字符串集合类型
     * 不为空转换，否使用默认
     * 使用“,”作为分隔符
     * @param value
     * @param defaultValue
     * @return
     */
    protected Set<String> stringSetValueOf(String value, String defaultValue) {
        value = value == null ? defaultValue : value;
        return new HashSet<>(Arrays.asList(value.split(",")));
    }

    /**
     * 将别名转为数据库类型
     * @param alias
     * @return
     */
    protected JdbcType resolveJdbcType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return JdbcType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
        }
    }

    /**
     * 将别名转化为结果集类型
     * @param alias
     * @return
     */
    protected ResultSetType resolveResultSetType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return ResultSetType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving ResultSetType. Cause: " + e, e);
        }
    }

    /**
     * 将别名转化为参数模型
     * @param alias
     * @return
     */
    protected ParameterMode resolveParameterMode(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return ParameterMode.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving ParameterMode. Cause: " + e, e);
        }
    }

    /**
     * 活动类型通过默认构造方法进行实例
     * @param alias
     * @return
     */
    protected Object createInstance(String alias) {
        Class<?> clazz = resolveClass(alias);
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BuilderException("Error creating instance. Cause: " + e, e);
        }
    }

    /**
     * 解析别名对应类型
     * @param alias
     * @param <T>
     * @return
     */
    protected <T> Class<? extends T> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) {
        if (typeHandlerAlias == null) {
            return null;
        }
        Class<?> type = resolveClass(typeHandlerAlias);
        if (type != null && !TypeHandler.class.isAssignableFrom(type)) {
            throw new BuilderException("Type " + type.getName() + " is not a valid TypeHandler because it does not implement TypeHandler interface");
        }
        @SuppressWarnings("unchecked") // already verified it is a TypeHandler
                Class<? extends TypeHandler<?>> typeHandlerType = (Class<? extends TypeHandler<?>>) type;
        return resolveTypeHandler(javaType, typeHandlerType);
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null) {
            return null;
        }
        // javaType ignored for injected handlers see issue #746 for full detail
        TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
        if (handler == null) {
            // not in registry, create a new one
            handler = typeHandlerRegistry.getInstance(javaType, typeHandlerType);
        }
        return handler;
    }

    /**
     * 通过别名注册器
     * 查找别名对应的类型
     * @param alias
     * @param <T>
     * @return
     */
    protected <T> Class<? extends T> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }
}
