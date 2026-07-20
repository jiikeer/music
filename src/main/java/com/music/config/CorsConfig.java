package com.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 全局跨域配置类
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 改为 /** 匹配所有接口，不再限制/api前缀
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080") // 前端地址
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS") // 必须加OPTIONS预检请求
                .allowCredentials(true)
                .maxAge(3600);
    }
}