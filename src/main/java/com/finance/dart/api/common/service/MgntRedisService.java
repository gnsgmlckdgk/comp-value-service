package com.finance.dart.api.common.service;


import com.finance.dart.api.common.dto.MgntRedisReqDto;
import com.finance.dart.api.common.dto.MgntRedisResDto;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MgntRedisService {

    private final RedisComponent redisComponent;


    /**
     * Redis 관리 서비스
     */
    public MgntRedisResDto reidsMgntService(MgntRedisReqDto mgntRedisReqDto) {

        String key = StringUtil.defaultString(mgntRedisReqDto.getKey());
        String value = StringUtil.defaultString(mgntRedisReqDto.getValue());
        Long ttl = mgntRedisReqDto.getTtl() == null ? 0 : mgntRedisReqDto.getTtl();
        String type = StringUtil.defaultString(mgntRedisReqDto.getType());

        boolean isSuccess = true;

        String result = switch (type) {
            case "I" -> {
                if(ttl == 0) redisComponent.saveValue(key, value);
                else redisComponent.saveValueWithTtl(key, value, ttl);
                yield "[" + key + "] 등록완료";
            }
            case "S" -> redisComponent.getValue(key);
            case "D" -> {
                redisComponent.deleteKey(key);
                yield "[" + key + "] 삭제완료";
            }
            case "PS" -> redisComponent.scanKeys(key).toString();
            case "PD" -> {
                redisComponent.deleteKeys(key);
                yield "[" + key + "] 삭제완료";
            }
            default -> {
                isSuccess = false;
                yield "타입값이 올바르지 않습니다.";
            }
        };

        return new MgntRedisResDto(isSuccess, result);
    }

}
