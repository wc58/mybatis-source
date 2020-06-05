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
package org.apache.ibatis.builder;

import org.apache.ibatis.cache.Cache;

/**
 * 缓存建造器
 */
public class CacheRefResolver {
  //mapper建造器的助理
  private final MapperBuilderAssistant assistant;
  //缓存的命名空间
  private final String cacheRefNamespace;

  /**
   * 初始化
   *  mapper建造器的助理
   *   缓存的命名空间
   * @param assistant
   * @param cacheRefNamespace
   */
  public CacheRefResolver(MapperBuilderAssistant assistant, String cacheRefNamespace) {
    this.assistant = assistant;
    this.cacheRefNamespace = cacheRefNamespace;
  }

  /**
   * 根据缓存的命名空间取出缓存
   * 在缓存建造器创建时，缓存就已确定
   * @return
   */
  public Cache resolveCacheRef() {
    return assistant.useCacheRef(cacheRefNamespace);
  }
}