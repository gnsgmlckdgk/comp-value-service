package com.finance.dart.common.interceptor;

import com.finance.dart.member.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@AllArgsConstructor
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(log.isDebugEnabled()) log.debug("선처리");

        //@ 세션 확인
        sessionService.sessionCheckErrResponse(request, response);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);

        if(log.isDebugEnabled()) log.debug("후처리");

    }
}