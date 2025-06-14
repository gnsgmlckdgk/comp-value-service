package com.finance.dart.api.controller;

import com.finance.dart.api.dto.DisclosuerInfoReqDTO;
import com.finance.dart.api.service.CorpCodeService;
import com.finance.dart.api.service.DisclosuerInfoService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 공시정보 컨트롤러
 */

@RestController
@AllArgsConstructor
@RequestMapping("disclosure")
public class DisclosureController {

    private final DisclosuerInfoService disclosuerInfoService;
    private final CorpCodeService corpCodeService;


    /**
     * 공시정보 목록 조회
     * @param disclosuerInfoReqDTO
     * @return
     */
    @PostMapping("/disc/list")
    public ResponseEntity<Object> getDisclosureInfoList(@RequestBody DisclosuerInfoReqDTO disclosuerInfoReqDTO) {
        return new ResponseEntity<>(disclosuerInfoService.getDisclosuerInfo(disclosuerInfoReqDTO), HttpStatus.OK);
    }

    /**
     * 고유번호 조회
     * @return
     */
    @GetMapping("corpCode")
    public ResponseEntity<Object> getCorpCode() {

        return new ResponseEntity<>(corpCodeService.getCorpCode(true), HttpStatus.OK);
    }

}
