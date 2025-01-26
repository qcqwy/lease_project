package com.atguigu.lease.web.app.service.impl;

import com.aliyun.teaopenapi.Client;
import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.CodeUtil;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.SmsService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    SmsService smsService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    UserInfoService userInfoService;
    @Override
    public void getSMSCode(String phone) {
        //生成验证码
        String code = CodeUtil.getRandomCode(6);

        String key = RedisConstant.APP_LOGIN_PREFIX + phone;
        //先判断手机号是否为空
        if(phone == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        //2. 检查Redis中是否已经存在该手机号码的key
        Boolean hasKey = redisTemplate.hasKey(key);
        if(hasKey){
            //若存在，则判断验证码是否发送超1分钟
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if(RedisConstant.APP_LOGIN_CODE_TTL_SEC - expire < RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC){
                //若发送小于一分钟，验证码发送过于频繁
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }
        //3.发送短信验证码
        smsService.sendCode(phone, code);
        redisTemplate.opsForValue().set(key, code, RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public String login(LoginVo loginVo) {
        //1.判断手机号码和验证码是否为空
        if(loginVo.getPhone() == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        if(loginVo.getCode() == null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }
        //2.校验验证码
        String code = redisTemplate.opsForValue().get(RedisConstant.APP_LOGIN_PREFIX + loginVo.getPhone());
        if(code == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        if(!code.equals(loginVo.getCode())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }
        //3.判断用户是否存在,不存在则注册（创建用户）
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getPhone, loginVo.getPhone());
        UserInfo userInfo = userInfoService.getOne(wrapper);
        if (userInfo == null){
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setNickname("用户-"+loginVo.getPhone().substring(6));
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfoService.save(userInfo);
        }
        //4.判断用户是否被禁
        if(userInfo.getStatus().equals(BaseStatus.DISABLE)){
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }
        //5.创建并返回token
        return JwtUtil.createToken(userInfo.getId(), userInfo.getPhone());


    }

    @Override
    public UserInfoVo getLoginUserVoById(Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        UserInfoVo userInfoVo = new UserInfoVo(userInfo.getNickname(),userInfo.getAvatarUrl());
        return userInfoVo;
    }

}
