package com.finance.dart.common.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public class ConvertUtil {

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
