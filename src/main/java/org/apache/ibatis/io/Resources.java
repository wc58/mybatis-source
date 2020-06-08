/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * 用于加载文件
 */
public class Resources {

  // 类加载器的集合
  private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

  //字符集
  private static Charset charset;

  Resources() {
  }

  /**
   * 获取默认类加载器
   * @return
   */
  public static ClassLoader getDefaultClassLoader() {
    return classLoaderWrapper.defaultClassLoader;
  }

  /**
   * 设置默认类加载器
   * @param defaultClassLoader
   */
  public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
    classLoaderWrapper.defaultClassLoader = defaultClassLoader;
  }

  /**
   * 将类路径转换为url路径
   * @param resource
   * @return
   * @throws IOException
   */
  public static URL getResourceURL(String resource) throws IOException {
    // issue #625
    return getResourceURL(null, resource);
  }

  /**
   * 设置一个类加载器，将类路径转换为url路径
   * @param loader
   * @param resource
   * @return
   * @throws IOException
   */
  public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
    URL url = classLoaderWrapper.getResourceAsURL(resource, loader);
    if (url == null) {
      throw new IOException("Could not find resource " + resource);
    }
    return url;
  }

  /**
   * 将类路径文件转换为一个输入流
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static InputStream getResourceAsStream(String resource) throws IOException {
    return getResourceAsStream(null, resource);
  }

  /**
   * 使用指定类加载器，将类路径文件转换为一个输入流
   *
   * @param loader
   * @param resource
   * @return
   */
  public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
    InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
    if (in == null) {
      throw new IOException("Could not find resource " + resource);
    }
    return in;
  }

  /**
   * 将类路径文件转换为一个输入流，读取到Properties对象中
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Properties getResourceAsProperties(String resource) throws IOException {
    Properties props = new Properties();
    try (InputStream in = getResourceAsStream(resource)) {
      props.load(in);
    }
    return props;
  }

  /**
   * 设置一个类加载器，将类路径文件转换为一个输入流，读取到Properties对象中
   *
   * @param loader   The classloader used to fetch the resource
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
    Properties props = new Properties();
    try (InputStream in = getResourceAsStream(loader, resource)) {
      props.load(in);
    }
    return props;
  }

  /**
   *  将类路径文件转换为一个输入流，再封装为InputStreamReader对象
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Reader getResourceAsReader(String resource) throws IOException {
    Reader reader;
    if (charset == null) {
      reader = new InputStreamReader(getResourceAsStream(resource));
    } else {
      reader = new InputStreamReader(getResourceAsStream(resource), charset);
    }
    return reader;
  }

  /**
   * 设置一个类加载器，将类路径文件转换为一个输入流，再封装为InputStreamReader对象
   *
   * @param loader   The classloader used to fetch the resource
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
    Reader reader;
    if (charset == null) {
      reader = new InputStreamReader(getResourceAsStream(loader, resource));
    } else {
      reader = new InputStreamReader(getResourceAsStream(loader, resource), charset);
    }
    return reader;
  }

  /**
   *  将类路径转换为url路径，读取成file文件
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static File getResourceAsFile(String resource) throws IOException {
    return new File(getResourceURL(resource).getFile());
  }

  /**
   * 设置一个类加载器，将类路径转换为url路径，读取成file文件
   *
   * @param loader   - the classloader used to fetch the resource
   * @param resource - the resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
    return new File(getResourceURL(loader, resource).getFile());
  }

  /**
   * 获取作为输入流的URL
   *
   * @param urlString - the URL to get
   * @return An input stream with the data from the URL
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static InputStream getUrlAsStream(String urlString) throws IOException {
    URL url = new URL(urlString);
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  /**
   * 获取作为读取器的URL
   *
   * @param urlString - the URL to get
   * @return A Reader with the data from the URL
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Reader getUrlAsReader(String urlString) throws IOException {
    Reader reader;
    if (charset == null) {
      reader = new InputStreamReader(getUrlAsStream(urlString));
    } else {
      reader = new InputStreamReader(getUrlAsStream(urlString), charset);
    }
    return reader;
  }

  /**
   * 加载Properties文件
   * @param urlString
   * @return
   * @throws IOException
   */
  public static Properties getUrlAsProperties(String urlString) throws IOException {
    Properties props = new Properties();
    try (InputStream in = getUrlAsStream(urlString)) {
      props.load(in);
    }
    return props;
  }

  /**
   * 使用多种类加载器，将一个全类名方式加载
   * @param className
   * @return
   * @throws ClassNotFoundException
   */
  public static Class<?> classForName(String className) throws ClassNotFoundException {
    return classLoaderWrapper.classForName(className);
  }

  /**
   * 获得字符集
   * @return
   */
  public static Charset getCharset() {
    return charset;
  }

  /**
   * 设置字符集
   * @param charset
   */
  public static void setCharset(Charset charset) {
    Resources.charset = charset;
  }

}
