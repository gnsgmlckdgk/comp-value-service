package com.finance.dart.api.common.context;

import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 백그라운드 스레드에서 request-scope 빈(RequestContext 등)을 사용하기 위한 경량 RequestAttributes.
 * - 스케줄러/전수평가처럼 HTTP 요청이 없는 스레드에서 request 스코프를 임시로 활성화한다.
 * - request 스코프 속성만 지원 (session 스코프 미사용).
 * - 정상 HTTP 요청 1건 = 이 인스턴스 1개 (동일 데이터 격리 수준).
 */
public class SimpleRequestAttributes implements RequestAttributes {

    private final Map<String, Object> requestAttributes = new LinkedHashMap<>();
    private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>();
    private volatile boolean completed = false;

    @Override
    @Nullable
    public Object getAttribute(String name, int scope) {
        if (scope == SCOPE_REQUEST) {
            return requestAttributes.get(name);
        }
        return null;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        if (scope == SCOPE_REQUEST) {
            requestAttributes.put(name, value);
        }
    }

    @Override
    public void removeAttribute(String name, int scope) {
        if (scope == SCOPE_REQUEST) {
            requestAttributes.remove(name);
            destructionCallbacks.remove(name);
        }
    }

    @Override
    public String[] getAttributeNames(int scope) {
        if (scope == SCOPE_REQUEST) {
            return requestAttributes.keySet().toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        if (scope == SCOPE_REQUEST) {
            destructionCallbacks.put(name, callback);
        }
    }

    @Override
    @Nullable
    public Object resolveReference(String key) {
        return null;  // request/session 객체 참조 미지원
    }

    @Override
    public String getSessionId() {
        return "background-" + System.identityHashCode(this);
    }

    @Override
    public Object getSessionMutex() {
        return this;
    }

    /**
     * 스코프 종료 시 등록된 소멸 콜백 실행 (RequestContextHolder.resetRequestAttributes 후 수동 호출용)
     */
    public void requestCompleted() {
        if (completed) return;
        completed = true;
        for (Runnable callback : destructionCallbacks.values()) {
            try {
                callback.run();
            } catch (Exception ignored) {
                // 소멸 콜백 실패는 무시 (정리 목적)
            }
        }
        destructionCallbacks.clear();
    }
}
