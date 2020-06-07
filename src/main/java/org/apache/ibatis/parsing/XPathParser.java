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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.ibatis.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XPathParser {

  //xml所对应的Document对象
  private final Document document;
  //验证？
  private boolean validation;
  //实体解析器
  private EntityResolver entityResolver;
  //配置属性
  private Properties variables;
  //解析xml
  private XPath xpath;

  public XPathParser(String xml) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document) {
    commonConstructor(false, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = document;
  }

  /**
   * 设置属性
   * @param variables
   */
  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  /**
   * 从本DOM树中查找指定字符串
   * @param expression
   * @return
   */
  public String evalString(String expression) {
    return evalString(document, expression);
  }

  /**
   * 从指定DOM树中查找指定字符串
   * @param root
   * @param expression
   * @return
   */
  public String evalString(Object root, String expression) {
    String result = (String) evaluate(expression, root, XPathConstants.STRING);
    result = PropertyParser.parse(result, variables);
    return result;
  }

  /**
   * 从本DOM树中查找指定布尔值
   * @param expression
   * @return
   */
  public Boolean evalBoolean(String expression) {
    return evalBoolean(document, expression);
  }

  /**
   * 从指定DOM树中查找指定布尔值
   * @param root
   * @param expression
   * @return
   */
  public Boolean evalBoolean(Object root, String expression) {
    return (Boolean) evaluate(expression, root, XPathConstants.BOOLEAN);
  }
  /**
   * 从本DOM树中查找指定短整型
   * @param expression
   * @return
   */
  public Short evalShort(String expression) {
    return evalShort(document, expression);
  }

  /**
   * 从指定DOM树中查找指定短整型
   * @param root
   * @param expression
   * @return
   */
  public Short evalShort(Object root, String expression) {
    return Short.valueOf(evalString(root, expression));
  }

  /**
   * 从本DOM树中查找指定整型
   * @param expression
   * @return
   */
  public Integer evalInteger(String expression) {
    return evalInteger(document, expression);
  }

  /**
   * 从指定DOM树中查找指定整型
   * @param root
   * @param expression
   * @return
   */
  public Integer evalInteger(Object root, String expression) {
    return Integer.valueOf(evalString(root, expression));
  }

  /**
   * 从本DOM树中查找指定长整型
   * @param expression
   * @return
   */
  public Long evalLong(String expression) {
    return evalLong(document, expression);
  }

  /**
   * 从指定DOM树中查找指定长整型
   * @param root
   * @param expression
   * @return
   */
  public Long evalLong(Object root, String expression) {
    return Long.valueOf(evalString(root, expression));
  }

  /**
   * 从本DOM树中查找指定单精度浮点数
   * @param expression
   * @return
   */
  public Float evalFloat(String expression) {
    return evalFloat(document, expression);
  }

  /**
   * 从指定DOM树中查找指定单精度浮点数
   * @param root
   * @param expression
   * @return
   */
  public Float evalFloat(Object root, String expression) {
    return Float.valueOf(evalString(root, expression));
  }

  /**
   * 从本DOM树中查找指定双精度浮点数
   * @param expression
   * @return
   */
  public Double evalDouble(String expression) {
    return evalDouble(document, expression);
  }

  /**
   * 从指定DOM树中查找指定双精度浮点数
   * @param root
   * @param expression
   * @return
   */
  public Double evalDouble(Object root, String expression) {
    return (Double) evaluate(expression, root, XPathConstants.NUMBER);
  }

  /**
   * 从本DOM树中查找指定节点
   * 并封装为XNode节点的集合
   * @param expression
   * @return
   */
  public List<XNode> evalNodes(String expression) {
    return evalNodes(document, expression);
  }

  /**
   * 从指定DOM树中查找指定节点
   * 并封装为XNode节点的集合
   * @param root
   * @param expression
   * @return
   */
  public List<XNode> evalNodes(Object root, String expression) {
    List<XNode> xnodes = new ArrayList<>();
    NodeList nodes = (NodeList) evaluate(expression, root, XPathConstants.NODESET);
    //遍历，封装，保存
    for (int i = 0; i < nodes.getLength(); i++) {
      xnodes.add(new XNode(this, nodes.item(i), variables));
    }
    return xnodes;
  }

  /**
   * 从本DOM树中查找指定节点
   * 并封装为XNode节点
   * @param expression
   * @return
   */
  public XNode evalNode(String expression) {
    return evalNode(document, expression);
  }

  /**
   * 从指定DOM树中查找指定节点
   * 并封装为XNode节点
   * @param root
   * @param expression
   * @return
   */
  public XNode evalNode(Object root, String expression) {
    Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
    if (node == null) {
      return null;
    }
    return new XNode(this, node, variables);
  }

  /**
   * 使用xpath通过表达式
   * 在指定DOM树中查找指定的类型值
   *
   * @param expression
   * @param root
   * @param returnType
   * @return
   */
  private Object evaluate(String expression, Object root, QName returnType) {
    try {
      return xpath.evaluate(expression, root, returnType);
    } catch (Exception e) {
      throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
    }
  }

  /**
   * 创建主配置文件的DOM树
   * @param inputSource
   * @return
   */
  private Document createDocument(InputSource inputSource) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setValidating(validation);

      factory.setNamespaceAware(false);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(false);
      factory.setCoalescing(false);
      factory.setExpandEntityReferences(true);

      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setEntityResolver(entityResolver);
      builder.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          // NOP
        }
      });
      return builder.parse(inputSource);
    } catch (Exception e) {
      throw new BuilderException("Error creating document instance.  Cause: " + e, e);
    }
  }

  /**
   * 初始化参数
   * @param validation 验证通过？
   * @param variables 属性
   * @param entityResolver 实体解析器
   */
  private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
    this.validation = validation;
    this.entityResolver = entityResolver;
    this.variables = variables;
    XPathFactory factory = XPathFactory.newInstance();
    //初始化Xpath
    this.xpath = factory.newXPath();
  }

}
