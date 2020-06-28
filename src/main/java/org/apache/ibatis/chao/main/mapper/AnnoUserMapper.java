package org.apache.ibatis.chao.main.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.chao.main.pojo.AnnoUser;

import java.util.List;

public interface AnnoUserMapper {
    @ConstructorArgs({
            @Arg(column = "password", name = "name", javaType = String.class),
            @Arg(column = "name", name = "password", javaType = String.class)

    })
    @Select("SELECT * FROM tb_user WHERE id = #{id}")
    AnnoUser getUserById(Integer id);


    @Select("SELECT * FROM tb_user")
    List<AnnoUser> getUser();

    @Insert("INSERT INTO  tb_user(id,name ,password,sex,age)" +
            "VALUES (#{id},#{name},#{password},#{sex},#{age})")
    void insertUser(AnnoUser user);

    @Update("SELECT * FROM tb_user")
    void updateUser(AnnoUser user);

    @Delete("DELETE FROM tb_user WHERE id = #{id}")
    void deleteUser(Integer id);

}
