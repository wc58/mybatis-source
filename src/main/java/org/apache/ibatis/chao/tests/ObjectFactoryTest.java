package org.apache.ibatis.chao.tests;

import org.apache.ibatis.chao.pojo.User;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.Arrays;

public class ObjectFactoryTest {

    public static void main(String[] args) {
        DefaultObjectFactory factory = new DefaultObjectFactory();
        User sss = factory.create(User.class, Arrays.asList(Integer.class), Arrays.asList(1));
        System.out.println(sss);
    }


}
