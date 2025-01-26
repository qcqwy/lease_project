/**
 * @program: lease
 * @description:
 * @author: LDY
 * @create: 2024-12-28 17:29
 **/
package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class LeaseException extends RuntimeException{
    private int code;
    //根据状态码和异常信息抛出异常
    public LeaseException(int code, String msg){
        super(msg);
        this.code = code;
    }
    //根据异常枚举类获取信息抛出异常
    public LeaseException(ResultCodeEnum codeEnum){
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
    }
    public String toString(){
        return "code=" + code + ", message="+this.getMessage();
    }

}
