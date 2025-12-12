package com.finance.dart.member.controller;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.member.entity.RoleEntity;
import com.finance.dart.member.enums.Role;
import com.finance.dart.member.service.RoleService;
import com.finance.dart.member.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RequestMapping("role")
@RestController
public class RoleController {

    private final RoleService roleService;

    private final SessionService sessionService;


    /**
     * 권한 목록 조회
     */
    @GetMapping
    public ResponseEntity<CommonResponse<List<RoleEntity>>> getRoles() {
        CommonResponse<List<RoleEntity>> response = roleService.getRoles();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 권한 단건 조회
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<CommonResponse<RoleEntity>> getRole(@PathVariable("roleId") Long roleId) {
        CommonResponse<RoleEntity> response = roleService.getRole(roleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 권한 등록
     */
    @PostMapping
    public ResponseEntity<CommonResponse<RoleEntity>> createRole(@RequestBody RoleEntity roleEntity) {
        CommonResponse<RoleEntity> response = roleService.createRole(roleEntity);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 권한 수정
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<CommonResponse<RoleEntity>> updateRole(
            @PathVariable("roleId") Long roleId,
            @RequestBody RoleEntity roleEntity) {
        CommonResponse<RoleEntity> response = roleService.updateRole(roleId, roleEntity);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 권한 삭제
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<CommonResponse<Void>> deleteRole(@PathVariable("roleId") Long roleId) {
        CommonResponse<Void> response = roleService.deleteRole(roleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 회원에게 권한 부여
     */
    @PostMapping("/member/{memberId}/role/{roleId}")
    public ResponseEntity<CommonResponse<Void>> assignRoleToMember(
            HttpServletRequest httpRequest,
            @PathVariable("memberId") Long memberId,
            @PathVariable("roleId") Long roleId) {

        // 슈퍼 관리자 권한 체크 (SUPER_ADMIN)
        if (!sessionService.hasRole(httpRequest, Role.SUPER_ADMIN.getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>(ResponseEnum.FORBIDDEN));
        }

        CommonResponse<Void> response = roleService.assignRoleToMember(memberId, roleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 회원의 권한 제거
     */
    @DeleteMapping("/member/{memberId}/role/{roleId}")
    public ResponseEntity<CommonResponse<Void>> removeRoleFromMember(
            HttpServletRequest httpRequest,
            @PathVariable("memberId") Long memberId,
            @PathVariable("roleId") Long roleId) {

        // 슈퍼 관리자 권한 체크 (SUPER_ADMIN)
        if (!sessionService.hasRole(httpRequest, Role.SUPER_ADMIN.getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>(ResponseEnum.FORBIDDEN));
        }

        CommonResponse<Void> response = roleService.removeRoleFromMember(memberId, roleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 회원의 권한 목록 조회
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<CommonResponse<List<String>>> getMemberRoles(
            HttpServletRequest httpRequest,
            @PathVariable("memberId") Long memberId
    ) {

        // 관리자 권한 체크 (ADMIN 또는 SUPER_ADMIN)
        if (!sessionService.hasRole(httpRequest, Role.ADMIN.getRoleName()) &&
                !sessionService.hasRole(httpRequest, Role.SUPER_ADMIN.getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>(ResponseEnum.FORBIDDEN));
        }

        CommonResponse<List<String>> response = roleService.getMemberRoles(memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
