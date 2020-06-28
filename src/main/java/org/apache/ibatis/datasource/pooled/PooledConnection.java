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
package org.apache.ibatis.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.reflection.ExceptionUtil;

/**
 * 一个普通连接的增强类
 */
class PooledConnection implements InvocationHandler {

    private static final String CLOSE = "close";
    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

    private final int hashCode;
    private final PooledDataSource dataSource;
    //真实连接
    private final Connection realConnection;
    //代理连接
    private final Connection proxyConnection;
    private long checkoutTimestamp;
    private long createdTimestamp;
    private long lastUsedTimestamp;
    private int connectionTypeCode;
    //有效？
    private boolean valid;

    /**
     * 设置实际连接和连接池
     * @param connection
     * @param dataSource
     */
    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.hashCode = connection.hashCode();
        this.realConnection = connection;
        this.dataSource = dataSource;
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.valid = true;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    /**
     * 设置无效
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * 是否有效
     * @return
     */
    public boolean isValid() {
        return valid && realConnection != null && dataSource.pingConnection(this);
    }

    /**
     * 获取真实连接
     * @return
     */
    public Connection getRealConnection() {
        return realConnection;
    }

    /**
     * 获取代理连接
     * @return
     */
    public Connection getProxyConnection() {
        return proxyConnection;
    }

    /**
     * 获取真实连接的哈希值
     * @return
     */
    public int getRealHashCode() {
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    /**
     * Getter for the connection type (based on url + user + password).
     *
     * @return The connection type
     */
    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    /**
     * Setter for the connection type.
     *
     * @param connectionTypeCode
     *          - the connection type
     */
    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    /**
     * Getter for the time that the connection was created.
     *
     * @return The creation timestamp
     */
    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Setter for the time that the connection was created.
     *
     * @param createdTimestamp
     *          - the timestamp
     */
    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /**
     * Getter for the time that the connection was last used.
     *
     * @return - the timestamp
     */
    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    /**
     * Setter for the time that the connection was last used.
     *
     * @param lastUsedTimestamp
     *          - the timestamp
     */
    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    /**
     * Getter for the time since this connection was last used.
     *
     * @return - the time since the last use
     */
    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }

    /**
     * Getter for the age of the connection.
     *
     * @return the age
     */
    public long getAge() {
        return System.currentTimeMillis() - createdTimestamp;
    }

    /**
     * Getter for the timestamp that this connection was checked out.
     *
     * @return the timestamp
     */
    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    /**
     * Setter for the timestamp that this connection was checked out.
     *
     * @param timestamp
     *          the timestamp
     */
    public void setCheckoutTimestamp(long timestamp) {
        this.checkoutTimestamp = timestamp;
    }

    /**
     * Getter for the time that this connection has been checked out.
     *
     * @return the time
     */
    public long getCheckoutTime() {
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Allows comparing this connection to another.
     *
     * @param obj
     *          - the other connection to test for equality
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return realConnection.hashCode() == ((PooledConnection) obj).realConnection.hashCode();
        } else if (obj instanceof Connection) {
            return hashCode == obj.hashCode();
        } else {
            return false;
        }
    }

    /**
     * 主要用于检查连接是否被关闭？放入空闲池：执行sql
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (CLOSE.equals(methodName)) {
            dataSource.pushConnection(this);
            return null;
        }
        try {
            if (!Object.class.equals(method.getDeclaringClass())) {
                checkConnection();
            }
            return method.invoke(realConnection, args);
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }

    }

    /**
     * 检查连接是否有效
     * @throws SQLException
     */
    private void checkConnection() throws SQLException {
        if (!valid) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }

}
