package org.apache.ibatis.chao.mapper;

import org.apache.ibatis.chao.pojo.User;

import java.util.List;

public interface UserMapper {

  User getUserById(Integer id);

  List<User> getUser();

  void insertUser(User user);

  void updateUser(User user);

  void deleteUser(Integer id);

}
