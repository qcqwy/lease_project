/**
 * @program: lease
 * @description: 添加自定义功能的配置类
 * @author: LDY
 * @create: 2024-12-26 20:45
 **/
package com.atguigu.lease.web.admin.custom.config;

import com.atguigu.lease.web.admin.custom.convert.StringToBaseEnumConvertFactory;
import com.atguigu.lease.web.admin.custom.convert.StringToItemTypeConvert;
import com.atguigu.lease.web.admin.custom.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
//    @Autowired
//    private StringToItemTypeConvert convert;
    @Autowired
    private StringToBaseEnumConvertFactory convertFactory;
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(authenticationInterceptor).addPathPatterns("/admin/**").excludePathPatterns("/admin/login/**");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
//        registry.addConverter(convert);
        registry.addConverterFactory(convertFactory);
    }
}
