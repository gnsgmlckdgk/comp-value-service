package com.finance.dart.api.common.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;

/**
 * Request Scope 데이터 저장 객체
 */
@Component
@RequestScope
public class RequestContext {

    private final Map<String, Object> attributes = new HashMap<>();

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        return (T) attributes.get(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public String getAttributeAsString(String key) {
        if(attributes.get(key) == null) return "";
        return String.valueOf(attributes.get(key));
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public void clear() {
        attributes.clear();
    }
}
