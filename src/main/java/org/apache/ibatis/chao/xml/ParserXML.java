package org.apache.ibatis.chao.xml;

import java.io.InputStream;

public class ParserXML {

  public static void main(String[] args) throws Exception {
    InputStream inputStream = ParserXML.class.getResourceAsStream("\\org\\apache\\ibatis\\chao\\mapper\\UserMapper.xml");
    System.out.println(inputStream);
  }

}
