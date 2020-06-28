package org.apache.ibatis.chao.main.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.annotations.Arg;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AnnoUser implements Serializable {

    private Integer id;
    private String name;
    private String password;
    private String sex;
    private Integer age;

    public AnnoUser(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public AnnoUser(Integer id) {
        this.id = id;
    }
}
