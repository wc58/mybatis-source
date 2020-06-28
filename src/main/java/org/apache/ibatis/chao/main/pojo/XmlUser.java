package org.apache.ibatis.chao.main.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class XmlUser implements Serializable {


    public XmlUser(String name, String password, String sex, Integer age) {
        this.name = name;
        this.password = password;
        this.sex = sex;
        this.age = age;
    }

    private Integer id;
    private String name;
    private String password;
    private String sex;
    private Integer age;


}
