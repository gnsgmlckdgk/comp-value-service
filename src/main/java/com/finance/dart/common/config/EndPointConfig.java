package com.finance.dart.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 엔드포인트 설정
 */
public class EndPointConfig {

    /**
     * <pre>
     * 로그인 필요없는 엔드포인트
     * @EndPointConfig.PublicEndpoint
     * </pre>
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PublicEndpoint {
        // 로그인 불필요
    }

    /**
     * <pre>
     * 권한 설정이 필요한 엔드포인트
     * @EndPointConfig.RequireRole({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
     * </pre>
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RequireRole {
        String[] value(); // {"ROLE_ADMIN", "ROLE_USER", "ROLE_SUPER_ADMIN"} : Role Enum 참고
    }

}
