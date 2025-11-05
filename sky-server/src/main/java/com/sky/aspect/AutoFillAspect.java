package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Date;

@Aspect
@Component
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..))&& @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 获取方法
        Method method = signature.getMethod();

        // 获取注解
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        if (autoFill != null) {
            System.out.println("检测到 AutoFill 注解，值为: " + autoFill.value());
        }
        Object[]args=joinPoint.getArgs();
        if(args==null||args.length==0) return ;
        Object employee= args[0];
        LocalDateTime date= LocalDateTime.now();
        Long id= BaseContext.getCurrentId();
        if(autoFill.value()== OperationType.INSERT){
            Method setCreateTime=employee.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
            Method setCreateUser=employee.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
            Method setUpdateTime=employee.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser=employee.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            setCreateUser.invoke(employee,id);
            setCreateTime.invoke(employee,date);
            setUpdateUser.invoke(employee,id);
            setUpdateTime.invoke(employee,date);
        }
        else if(autoFill.value()== OperationType.UPDATE){
            Method setUpdateTime=employee.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser=employee.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            setUpdateUser.invoke(employee,id);
            setUpdateTime.invoke(employee,date);
        }
    }

}
