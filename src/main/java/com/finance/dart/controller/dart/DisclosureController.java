package com.finance.dart.controller.dart;

import com.finance.dart.service.CorpCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공시정보 컨트롤러
 */

@RestController
@AllArgsConstructor
@RequestMapping("disclosure")
public class DisclosureController {

    private final CorpCodeService corpCodeService;

    /**
     * 고유번호 조회
     * @return
     */
    @GetMapping("corpCode")
    public ResponseEntity<Object> getCorpCode() {

        return new ResponseEntity<>(corpCodeService.getCorpCode(true), HttpStatus.OK);
    }

}
