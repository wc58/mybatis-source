/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.parsing;

/**
 * @author Clinton Begin
 */
public class GenericTokenParser {

  //开始符
  private final String openToken;
  //结束符
  private final String closeToken;
  //处理器
  private final TokenHandler handler;

  /**
   * 初始化一系列默认值
   * @param openToken
   * @param closeToken
   * @param handler
   */
  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  /**
   * 解析指定文本中的所有占位符
   *
   * @param text
   * @return
   */
  public String parse(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    int start = text.indexOf(openToken);
    if (start == -1) {
      return text;
    }
    char[] src = text.toCharArray();
    int offset = 0;
    final StringBuilder builder = new StringBuilder();
    StringBuilder expression = null;
    while (start > -1) {
      //是否有反斜杠，则当做普通字符串
      if (start > 0 && src[start - 1] == '\\') {
        builder.append(src, offset, start - offset - 1).append(openToken);
        offset = start + openToken.length();
      } else {
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        //第一次空
        builder.append(src, offset, start - offset);
        //偏移到开始符后面
        offset = start + openToken.length();
        //从指定索开始引查找指定值
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          //是否有反斜杠，则当做普通字符串
          if (end > offset && src[end - 1] == '\\') {
            expression.append(src, offset, end - offset - 1).append(closeToken);
            offset = end + closeToken.length();
            end = text.indexOf(closeToken, offset);
          } else {
            //取出表达式中内容
            expression.append(src, offset, end - offset);
            break;
          }
        }
        //是否有完整的占位符
        if (end == -1) {
          //否则把后面当做普通字符串
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          /*
          从之前的属性对象中查找该key对应的value
           */
          builder.append(handler.handleToken(expression.toString()));
          //表示第一个站位符已经解析完毕
          offset = end + closeToken.length();
        }
      }
      //尝试解析当前字符串中的第二个占位符
      start = text.indexOf(openToken, offset);
    }
    //检查占位符后面是否还有字符串，有则拼接
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
