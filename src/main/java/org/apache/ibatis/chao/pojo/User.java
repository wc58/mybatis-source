package org.apache.ibatis.chao.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class User implements Serializable {


  public User(Integer id,String name) {
    this.id = id;
    this.name =name;

  }

  private Integer id;
  private String name;
  private String password;
  private String sex;
  private Integer age;


}
