package com.finance.dart.common.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private CommonInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")        // 인터셉트할 경로
                .excludePathPatterns(
                        "/member/login", "/member/join", "/member/me",
                        "/member/find-usernames" , "/member/password/reset/request", "/member/password/reset/verify",
                        "/test/**");  // 예외
    }
}