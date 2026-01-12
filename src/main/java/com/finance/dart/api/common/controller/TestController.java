package com.finance.dart.api.common.controller;

import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.config.EndPointConfig;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

}
