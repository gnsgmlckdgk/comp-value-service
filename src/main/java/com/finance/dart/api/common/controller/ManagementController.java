package com.finance.dart.api.common.controller;


import com.finance.dart.api.common.dto.MgntRedisReqDto;
import com.finance.dart.api.common.dto.MgntRedisResDto;
import com.finance.dart.api.common.service.MgntRedisService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.member.enums.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("mgnt")
@RequiredArgsConstructor
public class ManagementController {

    private final MgntRedisService mgntRedisService;


    @EndPointConfig.RequireRole({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SUPER_ADMIN})
    @PostMapping("/redis")
    public ResponseEntity<CommonResponse<MgntRedisResDto>> redisMgnt(@RequestBody MgntRedisReqDto reqDto) {

        MgntRedisResDto mgntRedisResDto = mgntRedisService.reidsMgntService(reqDto);

        return new ResponseEntity<>(new CommonResponse<>(mgntRedisResDto), HttpStatus.OK);
    }

}
