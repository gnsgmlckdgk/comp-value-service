package com.finance.dart.api.controller;

import com.finance.dart.api.service.schedule.CalCompanyStockPerValueTotalService;
import com.finance.dart.common.service.RedisService;
import com.finance.dart.common.util.StringUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RequestMapping("test")
@AllArgsConstructor
@RestController
public class TestController {


    private final RedisService redisService;
    private final CalCompanyStockPerValueTotalService calCompanyStockPerValueTotalService;



    @GetMapping("/")
    public ResponseEntity<Object> test() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


    @PostMapping("/redis")
    public ResponseEntity<Object> redis(@RequestBody Map<String, Object> body) {

        String key = StringUtil.defaultString(body.get("key"));
        String value = StringUtil.defaultString(body.get("value"));
        String ttlStr = StringUtil.defaultString(body.get("ttl"));
        int ttl = "".equals(ttlStr) ? 0 : Integer.parseInt(ttlStr);
        String type = StringUtil.defaultString(body.get("type"));
        String pattern = StringUtil.defaultString(body.get("pattern"));

        String result = switch (type) {
            case "I" -> {
                if(ttl == 0) redisService.saveValue(key, value);
                else redisService.saveValueWithTtl(key, value, ttl);
                yield "등록완료";
            }
            case "S" -> redisService.getValue(key);
            case "D" -> {
                redisService.deleteKey(key);
                yield "삭제완료";
            }
            case "AK" -> redisService.scanKeys(pattern).toString();
            default -> "타입값이 올바르지 않습니다.";
        };

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/schedule")
    public ResponseEntity<Object> scheduleTest() {

        calCompanyStockPerValueTotalService.startScheduledTask();

        return new ResponseEntity<>("스케줄 실행됨!!(로그 확인)", HttpStatus.OK);
    }

}
