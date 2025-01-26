/**
 * @program: lease
 * @description:
 * @author: LDY
 * @create: 2024-12-29 01:08
 **/
package com.atguigu.lease.common.login;

import com.mysql.cj.log.Log;

public class LoginUserHolder {
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser) {
        threadLocal.set(loginUser);
    }

    public static LoginUser getLoginUser() {

        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}
