/**
 * @program: lease
 * @description: minio配置类
 * @author: LDY
 * @create: 2024-12-27 17:22
 **/
package com.atguigu.lease.common.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(name = "minio.endpoint")
public class MinioConfiguration {
    @Autowired
    MinioProperties properties;
    //把MinioClient做为组件加入到容器里
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().
                endpoint(properties.getEndpoint()).
                credentials(properties.getAccessKey(), properties.getSecretKey()).
                build();
    }
}
