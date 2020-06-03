package org.apache.ibatis.chao.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class User implements Serializable {

  private Integer id;
  private String name;
  private String password;
  private String sex;
  private Integer age;


}
