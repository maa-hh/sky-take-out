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
    //任意返回值类型的com.sky.mapper包下任意类 任意函数名任意参数
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
        Object object= args[0];//获取参数列表
        LocalDateTime date= LocalDateTime.now();
        Long id= BaseContext.getCurrentId();
        if(autoFill.value()== OperationType.INSERT){
            Method setCreateTime=object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
            Method setCreateUser=object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
            Method setUpdateTime=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            setCreateUser.invoke(object,id);
            setCreateTime.invoke(object,date);
            setUpdateUser.invoke(object,id);
            setUpdateTime.invoke(object,date);
        }
        else if(autoFill.value()== OperationType.UPDATE){
            Method setUpdateTime=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser=object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            setUpdateUser.invoke(object,id);
            setUpdateTime.invoke(object,date);
        }
    }

}
