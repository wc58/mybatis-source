/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.datasource.pooled;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * 用于产生具有连接池功能的数据源
 * 在创建Configuration类时，已经建该类注册到别名注册中心中
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

  /**
   * 覆盖父类里的普通数据源
   * 使用具有连接池功能的数据源
   */
  public PooledDataSourceFactory() {
    this.dataSource = new PooledDataSource();
  }

}
