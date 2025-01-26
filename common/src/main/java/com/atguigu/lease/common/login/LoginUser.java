/**
 * @program: lease
 * @description:
 * @author: LDY
 * @create: 2024-12-29 01:09
 **/
package com.atguigu.lease.common.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private String username;
}