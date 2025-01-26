package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    StringRedisTemplate template;
    @Autowired
    SystemUserMapper mapper;
    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        //获取验证码图片字符串
        String img = specCaptcha.toBase64();
        //获取验证码
        String code = specCaptcha.text().toLowerCase();
        //获取uuid
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        //给验证码设置60秒有效期存入redis中
        template.opsForValue().set(key, code, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);

        CaptchaVo captchaVo = new CaptchaVo(img, key);
        return captchaVo;
    }

    @Override
    public String login(LoginVo loginVo) {
        //验证码是否输入
        if(loginVo.getCaptchaCode() == null){
            throw  new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }
        //验证码是否已经过期
        String code = template.opsForValue().get(loginVo.getCaptchaKey());
        if(code == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        //验证验证码是否正确
        if(!code.equals(loginVo.getCaptchaCode().toLowerCase())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }
        //验证用户是否存在
        SystemUser user = mapper.selectOneByUsername(loginVo.getUsername());
        if(user == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        //验证用户状态
        if (user.getStatus() == BaseStatus.DISABLE){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }
        //验证用户密码是否正确

        if(!DigestUtils.md5Hex(loginVo.getPassword()).equals(user.getPassword())){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }
        //验证通过，创建jwt
        System.out.println(user.getId()+",,"+ user.getUsername());
        String token = JwtUtil.createToken(user.getId(), user.getUsername());
        return token;
    }

    @Override
    public SystemUserInfoVo getLoginUserInfo(Long userId) {

        return null;
    }
}
