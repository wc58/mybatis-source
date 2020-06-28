package org.apache.ibatis.chao.main.mapper;

import org.apache.ibatis.chao.main.pojo.XmlUser;

import java.util.List;

public interface XmlUserMapper {

    XmlUser getUserById(Integer id);

    List<XmlUser> getUser();

    void insertUser(XmlUser user);

    void updateUser(XmlUser user);

    void deleteUser(Integer id);

}
