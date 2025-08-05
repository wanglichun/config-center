package com.example.configcenter.config;

import com.example.configcenter.interceptor.ContextInterceptor;
import com.example.configcenter.interceptor.TraceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    ContextInterceptor contextInterceptor;
    @Autowired
    private TraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(contextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico");

        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico");
    }
} 