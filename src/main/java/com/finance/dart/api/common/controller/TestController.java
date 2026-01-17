package com.finance.dart.api.common.controller;

import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequestMapping("test")
@AllArgsConstructor
@RestController
public class TestController {

    private final RedisComponent redisComponent;


    @EndPointConfig.PublicEndpoint
    @GetMapping("/")
    public ResponseEntity<Object> test() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @EndPointConfig.PublicEndpoint
    @GetMapping("/redis/count")
    public ResponseEntity<String> count(@RequestParam(name = "key") String key) {

        String value = redisComponent.getValue(key);
        if(log.isDebugEnabled()) log.debug("key = {}", key);
        if(log.isDebugEnabled()) log.debug("value = {}", value);

        Map<String, Object> map = ConvertUtil.parseObject(value, Map.class);
        int count = 0;
        for(String mapKey : map.keySet()) {
            if(log.isDebugEnabled()) log.debug("1 depth key = {}", mapKey);
            count++;
        }

        return new ResponseEntity<>(StringUtil.defaultString(count), HttpStatus.OK);
    }

}
