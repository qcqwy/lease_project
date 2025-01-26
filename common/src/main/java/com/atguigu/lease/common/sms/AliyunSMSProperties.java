/**
 * @program: lease
 * @description:
 * @author: LDY
 * @create: 2025-01-21 17:16
 **/
package com.atguigu.lease.common.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSMSProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;

}
