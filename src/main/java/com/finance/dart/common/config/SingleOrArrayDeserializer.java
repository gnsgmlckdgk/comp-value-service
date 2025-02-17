package com.finance.dart.common.config;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * GSON 목록, 단일항목 같이 올수 있는 경우 목록으로 치환하는 설정
 * @param <T>
 */
public class SingleOrArrayDeserializer<T> implements JsonDeserializer<List<T>> {
    private final Class<T> clazz;

    public SingleOrArrayDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        List<T> list = new ArrayList<>();

        if (json.isJsonArray()) {
            // 배열인 경우
            JsonArray array = json.getAsJsonArray();
            for (JsonElement element : array) {
                list.add(context.deserialize(element, clazz));
            }
        } else if (json.isJsonObject()) {
            // 단일 객체인 경우, 하나의 항목으로 리스트에 추가
            list.add(context.deserialize(json, clazz));
        } else if (json.isJsonNull()) {
            // null인 경우 빈 리스트 반환
            return list;
        } else {
            throw new JsonParseException("Unexpected JSON type: " + json.getClass());
        }

        return list;
    }
}
