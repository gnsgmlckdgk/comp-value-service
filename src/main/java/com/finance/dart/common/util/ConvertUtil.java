package com.finance.dart.common.util;

import com.finance.dart.common.util.support.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ToNumberPolicy;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public class ConvertUtil {

    private static final Gson gson = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)   // 타입을 알수없을때 정수는 Long, 실수는 Double 로 파싱
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())   // gson은 LocalDateTime 타입 지원을 안해서 옵션 추가
            .create();

    /**
     * Gson 기반의 안전한 객체 변환 유틸
     * <pre>
     * - Object 또는 JSON 문자열을 받아 지정 타입으로 변환
     * - JSON 파싱 오류시 null 리턴 (예외 throw 안함)
     * - 필드명이 일치하는 항목만 자동 매핑
     * </pre>
     *
     * @param data  변환할 데이터 (Object 또는 JSON String)
     * @param clazz 변환할 타입
     * @return 변환된 객체 (파싱 실패시 null)
     */
    public static <T> T parseObject(Object data, Class<T> clazz) {

        if (data == null) return null;

        try {
            // data 가 이미 문자열인 경우
            if (data instanceof String jsonStr) {
                // 문자열이 JSON 형태인지 확인
                if (isJson(jsonStr)) {
                    return gson.fromJson(jsonStr, clazz);
                } else {
                    // JSON 형태가 아니면 변환 불가
                    return null;
                }
            }

            // 문자열이 아닌 일반 객체면
            String json = gson.toJson(data);
            return gson.fromJson(json, clazz);

        } catch (JsonSyntaxException e) {
            // 파싱 오류 발생 시 null 리턴 (또는 throw 로 바꿔도 됨)
            return null;
        }
    }

    /**
     * 문자열이 JSON 형식인지 확인 (Object/Array 둘 다 가능)
     */
    public static boolean isJson(String json) {
        if (json == null) return false;
        json = json.trim();
        return (json.startsWith("{") && json.endsWith("}"))
                || (json.startsWith("[") && json.endsWith("]"));
    }

    /**
     * DTO 객체를 Map<String, Object>로 변환
     *
     * @param dto DTO 객체
     * @param includeNull null 값 포함 여부 (true: 포함, false: 제외)
     * @return 변환된 Map
     */
    public static Map<String, Object> toMap(Object dto, boolean includeNull) {
        if (dto == null) return new HashMap<>();

        Map<String, Object> result = new HashMap<>();
        Class<?> clazz = dto.getClass();

        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(dto);

                    if (includeNull || value != null) {
                        result.put(field.getName(), value);
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("DTO 필드 접근 오류", e);
                }
            }
            clazz = clazz.getSuperclass();  // 상속 필드 포함
        }

        return result;
    }

    /**
     * Map<String, Object> → Map<String, String> 변환
     *
     * @param input        원본 Map
     * @param includeNull  null 값 포함 여부 (true: "" 문자열로 변환하여 포함, false: null 값 제거)
     * @return 변환된 Map<String, String>
     */
    public static Map<String, String> toStringMap(Map<String, Object> input, boolean includeNull) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : input.entrySet()) {
            Object value = entry.getValue();

            if (value == null) {
                if (includeNull) {
                    result.put(entry.getKey(), "");
                }
            } else {
                result.put(entry.getKey(), value.toString());
            }
        }

        return result;
    }

}
