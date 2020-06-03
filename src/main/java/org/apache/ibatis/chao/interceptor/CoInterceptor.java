package org.apache.ibatis.chao.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Method;

@Intercepts({@Signature(
  type= Executor.class,
  method = "update",
  args = {MappedStatement.class,Object.class})})
public class CoInterceptor implements Interceptor {
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Method method = invocation.getMethod();
    Object target = invocation.getTarget();
    Object proceed = invocation.proceed();
    Object[] args = invocation.getArgs();
    System.out.println(method);
    System.out.println(target);
    System.out.println(proceed);
    System.out.println(args);
    return invocation.proceed();
  }

 /* @Override
  public Object plugin(Object target) {
    return null;
  }

  @Override
  public void setProperties(Properties properties) {

  }*/
}
