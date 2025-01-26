/**
 * @program: lease
 * @description:
 * @author: LDY
 * @create: 2024-12-29 00:59
 **/
package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static SecretKey tokenSingKey = Keys.hmacShaKeyFor("M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ".getBytes());
    private static long tokenExpiration = 60 * 60 * 1000 * 24 * 365L;
    public static String createToken(Long userId, String username){
        String token = Jwts.builder().setSubject("USER_INFO").setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).
                claim("userId", userId).
                claim("username", username).
                signWith(tokenSingKey).
                compact();
        return token;
    }
    public static Claims parseToken(String token){
        //判断是否有token
        if(token == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        //返回Claims给拦截器使用
        try {
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(tokenSingKey).build();
            return jwtParser.parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }


    public static void main(String[] args) {
        String token = createToken(1L, "13888888888");
        System.out.println(token);
    }
}
