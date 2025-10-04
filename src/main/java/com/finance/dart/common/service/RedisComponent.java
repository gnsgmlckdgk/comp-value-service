package com.finance.dart.common.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisComponent {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 데이터 저장
     * @param key
     * @param value
     */
    public void saveValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 데이터 저장 (만료시간)
     * @param key
     * @param value
     * @param timeoutInSeconds
     */
    public void saveValueWithTtl(String key, String value, long timeoutInSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutInSeconds, TimeUnit.SECONDS);
    }

    /**
     * 데이터 저장 (만료시간)
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     */
    public void saveValueWithTtl(String key, String value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }


    /**
     * 데이터 조회
     * @param key
     * @return
     */
    // 조회
    public String getValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 데이터 삭제
     * @param key
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }


    /**
     * 패턴 키 조회
     * @param pattern
     * @return
     */
    public Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection()
                .scan(ScanOptions.scanOptions().match(pattern).count(1000).build());

        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }

        return keys;
    }

}
