package com.finance.dart.api.common.controller;

import com.finance.dart.api.common.service.schedule.RecommendedStocks;
import com.finance.dart.api.domestic.service.schedule.CalCompanyStockPerValueTotalService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.component.RedisComponent;
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


    private final RedisComponent redisComponent;
    private final CalCompanyStockPerValueTotalService calCompanyStockPerValueTotalService;
    private final RecommendedStocks recommendedStocks;



    @EndPointConfig.PublicEndpoint
    @GetMapping("/")
    public ResponseEntity<Object> test() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


    @EndPointConfig.PublicEndpoint
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
                if(ttl == 0) redisComponent.saveValue(key, value);
                else redisComponent.saveValueWithTtl(key, value, ttl);
                yield "등록완료";
            }
            case "S" -> redisComponent.getValue(key);
            case "D" -> {
                redisComponent.deleteKey(key);
                yield "삭제완료";
            }
            case "AK" -> redisComponent.scanKeys(pattern).toString();
            default -> "타입값이 올바르지 않습니다.";
        };

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @EndPointConfig.PublicEndpoint
    @GetMapping("/schedule")
    public ResponseEntity<Object> scheduleTest() {

        calCompanyStockPerValueTotalService.startScheduledTask();

        return new ResponseEntity<>("스케줄 실행됨!!(로그 확인)", HttpStatus.OK);
    }

    /**
     * 추천 종목 스케줄러 테스트
     * @param body maxCount: 최대 처리 건수 (기본값 5, 0이면 전체)
     * @return 스케줄 실행 메시지
     */
    @EndPointConfig.PublicEndpoint
    @PostMapping("/recommended-stocks")
    public ResponseEntity<Object> recommendedStocksTest(@RequestBody(required = false) Map<String, Object> body) {

        int maxCount = 5; // 테스트 기본값
        if (body != null && body.get("maxCount") != null) {
            maxCount = Integer.parseInt(StringUtil.defaultString(body.get("maxCount")));
        }

        recommendedStocks.startScheduledTask(maxCount);

        return new ResponseEntity<>("추천종목 스케줄 실행됨!! (maxCount=" + maxCount + ", 로그 확인)", HttpStatus.OK);
    }

}
