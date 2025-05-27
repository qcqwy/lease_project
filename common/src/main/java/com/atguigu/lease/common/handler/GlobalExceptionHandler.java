/**
 * @program: lease
 * @description:
 * @author: LDY
 * @create: 2024-12-28 10:56
 **/
package com.atguigu.lease.common.handler;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exception(Exception e){
        //专门处理触发器抛出的SQLException异常
        if(e.getCause() instanceof SQLException){
            SQLException sqlException = (SQLException) e.getCause();
            return Result.build(201, sqlException.getMessage());
        }

        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result LeaseException(LeaseException e){
        return Result.build(e.getCode(), e.getMessage());
    }
}
