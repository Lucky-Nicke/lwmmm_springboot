package com.lanxige.exception;

import com.lanxige.util.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.security.access.AccessDeniedException;

import javax.security.auth.login.LoginException;

@ControllerAdvice
public class GlobalException {
    //1.全局异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        System.out.println("全局执行....");
        e.printStackTrace();
        return Result.fail().message(e.getMessage());
    }

    //2.特定异常处理
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e) {
        System.out.println("特定执行.");
        e.printStackTrace();
        return Result.fail().message("执行了特定异常处理..");
    }

    //3.执行自定义异常
    @ExceptionHandler(MyCustomerException.class)
    @ResponseBody
    public Result error(MyCustomerException e) {
        e.printStackTrace();
        return Result.fail().code(e.getCode()).message(e.getMsg());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) {
        e.printStackTrace();
        return Result.fail().code(20001).message("拒绝操作没有权限");
    }

    @ExceptionHandler(LoginException.class)
    @ResponseBody
    public Result error(LoginException e) {
        System.out.println("未登录异常执行....");
        e.printStackTrace();
        return Result.fail().message(e.getMessage());
    }
}
