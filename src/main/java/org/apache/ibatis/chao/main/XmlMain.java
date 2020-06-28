package org.apache.ibatis.chao.main;


import org.apache.ibatis.chao.main.mapper.XmlUserMapper;
import org.apache.ibatis.chao.main.pojo.XmlUser;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class XmlMain {


    public static void main(String[] args) throws IOException {
        InputStream resource = Resources.getResourceAsStream("mybatis.xml");
        SqlSessionFactory build = new SqlSessionFactoryBuilder().build(resource);


        for (int i = 0; i < 10; i++) {
            SqlSession session = build.openSession(true);
            XmlUserMapper userMapper = session.getMapper(XmlUserMapper.class);
            XmlUser user = userMapper.getUserById(5);
            session.close();
        }

        SqlSession session = build.openSession(true);
        XmlUserMapper userMapper = session.getMapper(XmlUserMapper.class);
        //增
//        userMapper.insertUser(new XmlUser("chao", "123", "男", 16));

        //删
//    userMapper.deleteUser(5);
        //查
        XmlUser user = userMapper.getUserById(2);
//    User o = session.selectOne("org.apache.ibatis.chao.main.mapper.UserMapper.getUserById", 1);
//    System.out.println(o);
        //改
        user.setName("超哥哥");
        userMapper.updateUser(user);
    }
}
