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
package org.apache.ibatis.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 对普通的节点进一次封装
 */
public class XNode {

    //原始节点对象
    private final Node node;
    //原始节点名称
    private final String name;
    //正文
    private final String body;
    //元素属性
    private final Properties attributes;
    //属性
    private final Properties variables;
    //该节点对应的解析器
    private final XPathParser xpathParser;

    /**
     * 初始化属性
     * @param xpathParser
     * @param node
     * @param variables
     */
    public XNode(XPathParser xpathParser, Node node, Properties variables) {
        this.xpathParser = xpathParser;
        this.node = node;
        this.name = node.getNodeName();
        this.variables = variables;
        this.attributes = parseAttributes(node);
        this.body = parseBody(node);
    }

    /**
     * 再创建和当前属性一致的x节点
     * @param node
     * @return
     */
    public XNode newXNode(Node node) {
        return new XNode(xpathParser, node, variables);
    }

    /**
     * 获取父节点
     *
     *      通过原生获取父节点后，再封装返回
     * @return
     */
    public XNode getParent() {
        Node parent = node.getParentNode();
        if (!(parent instanceof Element)) {
            return null;
        } else {
            return new XNode(xpathParser, parent, variables);
        }
    }

    /**
     * 获取当前对象的路径（父节点 + 本身）
     *
     *      循环获取该节点的所有父节点的名称
     *      然后进行拼接返回
     * @return
     */
    public String getPath() {
        StringBuilder builder = new StringBuilder();
        Node current = node;
        while (current instanceof Element) {
            if (current != node) {
                builder.insert(0, "/");
            }
            builder.insert(0, current.getNodeName());
            current = current.getParentNode();
        }
        return builder.toString();
    }

    /**
     * 获取该节点的基本标识
     *
     *        将节点名称，对应的id，value，property值
     *        进行拼接，形成了唯一的标识
     * @return
     */
    public String getValueBasedIdentifier() {
        StringBuilder builder = new StringBuilder();
        XNode current = this;
        while (current != null) {
            if (current != this) {
                builder.insert(0, "_");
            }
            String value = current.getStringAttribute("id",
                    current.getStringAttribute("value",
                            current.getStringAttribute("property", (String) null)));
            if (value != null) {
                value = value.replace('.', '_');
                builder.insert(0, "]");
                builder.insert(0,
                        value);
                builder.insert(0, "[");
            }
            builder.insert(0, current.getName());
            current = current.getParent();
        }
        return builder.toString();
    }

    public String evalString(String expression) {
        return xpathParser.evalString(node, expression);
    }

    public Boolean evalBoolean(String expression) {
        return xpathParser.evalBoolean(node, expression);
    }

    public Double evalDouble(String expression) {
        return xpathParser.evalDouble(node, expression);
    }

    /**
     * 在该节点的基础下寻找指定的节点集合
     * @param expression
     * @return
     */
    public List<XNode> evalNodes(String expression) {
        return xpathParser.evalNodes(node, expression);
    }

    /**
     * 在该节点的基础下寻找指定的节点
     * @param expression
     * @return
     */
    public XNode evalNode(String expression) {
        return xpathParser.evalNode(node, expression);
    }

    public Node getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    public String getStringBody() {
        return getStringBody(null);
    }

    public String getStringBody(String def) {
        if (body == null) {
            return def;
        } else {
            return body;
        }
    }

    public Boolean getBooleanBody() {
        return getBooleanBody(null);
    }

    public Boolean getBooleanBody(Boolean def) {
        if (body == null) {
            return def;
        } else {
            return Boolean.valueOf(body);
        }
    }

    public Integer getIntBody() {
        return getIntBody(null);
    }

    public Integer getIntBody(Integer def) {
        if (body == null) {
            return def;
        } else {
            return Integer.parseInt(body);
        }
    }

    public Long getLongBody() {
        return getLongBody(null);
    }

    public Long getLongBody(Long def) {
        if (body == null) {
            return def;
        } else {
            return Long.parseLong(body);
        }
    }

    public Double getDoubleBody() {
        return getDoubleBody(null);
    }

    public Double getDoubleBody(Double def) {
        if (body == null) {
            return def;
        } else {
            return Double.parseDouble(body);
        }
    }

    public Float getFloatBody() {
        return getFloatBody(null);
    }

    public Float getFloatBody(Float def) {
        if (body == null) {
            return def;
        } else {
            return Float.parseFloat(body);
        }
    }

    public <T extends Enum<T>> T getEnumAttribute(Class<T> enumType, String name) {
        return getEnumAttribute(enumType, name, null);
    }

    public <T extends Enum<T>> T getEnumAttribute(Class<T> enumType, String name, T def) {
        String value = getStringAttribute(name);
        if (value == null) {
            return def;
        } else {
            return Enum.valueOf(enumType, value);
        }
    }

    /**
     * Return a attribute value as String.
     *
     * <p>
     * If attribute value is absent, return value that provided from supplier of default value.
     *
     * @param name
     *          attribute name
     * @param defSupplier
     *          a supplier of default value
     * @return the string attribute
     * @since 3.5.4
     */
    public String getStringAttribute(String name, Supplier<String> defSupplier) {
        String value = attributes.getProperty(name);
        return value == null ? defSupplier.get() : value;
    }

    /**
     * 获取指定属性的值
     * @param name
     * @return
     */
    public String getStringAttribute(String name) {
        return getStringAttribute(name, (String) null);
    }

    /**
     * 获取指定属性的值 ，没有则默认值
     * @param name
     * @return
     */
    public String getStringAttribute(String name, String def) {
        String value = attributes.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return value;
        }
    }

    public Boolean getBooleanAttribute(String name) {
        return getBooleanAttribute(name, null);
    }

    /**
     * 名称获取对应的布尔，没有则默认
     * @param name
     * @param def
     * @return
     */
    public Boolean getBooleanAttribute(String name, Boolean def) {
        String value = attributes.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return Boolean.valueOf(value);
        }
    }

    public Integer getIntAttribute(String name) {
        return getIntAttribute(name, null);
    }

    public Integer getIntAttribute(String name, Integer def) {
        String value = attributes.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return Integer.parseInt(value);
        }
    }

    public Long getLongAttribute(String name) {
        return getLongAttribute(name, null);
    }

    public Long getLongAttribute(String name, Long def) {
        String value = attributes.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return Long.parseLong(value);
        }
    }

    public Double getDoubleAttribute(String name) {
        return getDoubleAttribute(name, null);
    }

    public Double getDoubleAttribute(String name, Double def) {
        String value = attributes.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return Double.parseDouble(value);
        }
    }

    public Float getFloatAttribute(String name) {
        return getFloatAttribute(name, null);
    }

    public Float getFloatAttribute(String name, Float def) {
        String value = attributes.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return Float.parseFloat(value);
        }
    }

    public List<XNode> getChildren() {
        List<XNode> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList != null) {
            for (int i = 0, n = nodeList.getLength(); i < n; i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(new XNode(xpathParser, node, variables));
                }
            }
        }
        return children;
    }

    public Properties getChildrenAsProperties() {
        Properties properties = new Properties();
        for (XNode child : getChildren()) {
            String name = child.getStringAttribute("name");
            String value = child.getStringAttribute("value");
            if (name != null && value != null) {
                properties.setProperty(name, value);
            }
        }
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, 0);
        return builder.toString();
    }

    private void toString(StringBuilder builder, int level) {
        builder.append("<");
        builder.append(name);
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            builder.append(" ");
            builder.append(entry.getKey());
            builder.append("=\"");
            builder.append(entry.getValue());
            builder.append("\"");
        }
        List<XNode> children = getChildren();
        if (!children.isEmpty()) {
            builder.append(">\n");
            for (XNode child : children) {
                indent(builder, level + 1);
                child.toString(builder, level + 1);
            }
            indent(builder, level);
            builder.append("</");
            builder.append(name);
            builder.append(">");
        } else if (body != null) {
            builder.append(">");
            builder.append(body);
            builder.append("</");
            builder.append(name);
            builder.append(">");
        } else {
            builder.append("/>");
            indent(builder, level);
        }
        builder.append("\n");
    }

    private void indent(StringBuilder builder, int level) {
        for (int i = 0; i < level; i++) {
            builder.append("    ");
        }
    }

    /**
     * 解析属性
     *
     *        取出节点所有属性
     *        对值进行
     *        把属性名称和值对应到属性对象中返回
     * @param n
     * @return
     */
    private Properties parseAttributes(Node n) {
        Properties attributes = new Properties();
        NamedNodeMap attributeNodes = n.getAttributes();
        if (attributeNodes != null) {
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Node attribute = attributeNodes.item(i);
                String value = PropertyParser.parse(attribute.getNodeValue(), variables);
                attributes.put(attribute.getNodeName(), value);
            }
        }
        return attributes;
    }

    /**
     * 解析正文
     *
     *      解析后该节点后
     *      如果为空，则再继续解析其子节点，直到解析出正文
     * @param node
     * @return
     */
    private String parseBody(Node node) {
        String data = getBodyData(node);
        if (data == null) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                data = getBodyData(child);
                if (data != null) {
                    break;
                }
            }
        }
        return data;
    }

    /**
     * 获取正文
     *
     *      但节点必须是CDATASection或text类型的
     *      获取文本内容
     *
     *      最后返回
     * @param child
     * @return
     */
    private String getBodyData(Node child) {
        if (child.getNodeType() == Node.CDATA_SECTION_NODE
                || child.getNodeType() == Node.TEXT_NODE) {
            String data = ((CharacterData) child).getData();
            data = PropertyParser.parse(data, variables);
            return data;
        }
        return null;
    }

}
