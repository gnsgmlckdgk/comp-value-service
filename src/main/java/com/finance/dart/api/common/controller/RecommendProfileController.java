package com.finance.dart.api.common.controller;

import com.finance.dart.api.common.dto.RecommendProfileDto;
import com.finance.dart.api.common.service.RecommendProfileService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 추천종목 프로파일 컨트롤러
 */
@Slf4j
@AllArgsConstructor
@RequestMapping("recommend/profile")
@RestController
public class RecommendProfileController {

    private final RecommendProfileService recommendProfileService;

    /**
     * 프로파일 등록
     */
    @PostMapping("/regi")
    public ResponseEntity<CommonResponse<RecommendProfileDto>> regiProfile(@RequestBody RecommendProfileDto reqBody) {

        RecommendProfileDto result = recommendProfileService.regiProfile(reqBody);

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 프로파일 수정
     */
    @PostMapping("/modi")
    public ResponseEntity<CommonResponse<RecommendProfileDto>> modiProfile(@RequestBody RecommendProfileDto reqBody) {

        RecommendProfileDto result = recommendProfileService.modiProfile(reqBody);

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 프로파일 삭제
     */
    @PostMapping("/del")
    public ResponseEntity<CommonResponse<Void>> delProfile(@RequestBody RecommendProfileDto reqBody) {

        recommendProfileService.delProfile(reqBody.getId());

        return new ResponseEntity<>(new CommonResponse<>(), HttpStatus.OK);
    }

    /**
     * 프로파일 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<RecommendProfileDto>> getProfile(@PathVariable(name = "id") Long id) {

        RecommendProfileDto result = recommendProfileService.getProfile(id);

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 프로파일 목록 조회 (사용중인 프로파일)
     */
    @GetMapping("")
    public ResponseEntity<CommonResponse<List<RecommendProfileDto>>> getProfileList() {

        List<RecommendProfileDto> result = recommendProfileService.getProfileList();

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 활성화된 프로파일 목록 조회 (스케줄러용)
     */
    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<RecommendProfileDto>>> getActiveProfileList() {

        List<RecommendProfileDto> result = recommendProfileService.getActiveProfileList();

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 프로파일 활성화/비활성화 토글
     */
    @PostMapping("/toggle/{id}")
    public ResponseEntity<CommonResponse<RecommendProfileDto>> toggleActive(@PathVariable(name = "id") Long id) {

        RecommendProfileDto result = recommendProfileService.toggleActive(id);

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }
}
